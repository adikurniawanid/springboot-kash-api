package adi_kurniawan.springboot_kash_api.resolver;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.entity.UserToken;
import adi_kurniawan.springboot_kash_api.repository.UserRepository;
import adi_kurniawan.springboot_kash_api.repository.UserStatusRepository;
import adi_kurniawan.springboot_kash_api.repository.UserTokenRepository;
import adi_kurniawan.springboot_kash_api.service.TokenService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.MethodParameter;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.server.ResponseStatusException;

import java.util.Objects;

@Slf4j
@Component
public class UserArgumentResolver implements HandlerMethodArgumentResolver {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private UserStatusRepository userStatusRepository;
    @Autowired
    private TokenService tokenService;
    @Autowired
    private UserTokenRepository userTokenRepository;

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return User.class.equals(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest servletRequest = (HttpServletRequest) webRequest.getNativeRequest();
        String token = servletRequest.getHeader("Authorization");
        
        if (token == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized");
        }

        UserToken userToken = userTokenRepository.findFirstByAccessToken(token).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        );

        tokenService.validateToken(token);

        if (servletRequest.getRequestURI().equals("/api/auth/verify")) {
            return userToken.getUser();
        }

        if (Objects.isNull(userToken.getUser().getUserStatus().getEmailVerifiedAt())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email first");
        }

        if (servletRequest.getRequestURI().equals("/api/user/onboarding")) {
            return userToken.getUser();
        }

        if (Objects.isNull(userToken.getUser().getUserStatus().getOnboardedAt())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please do onboarding first");
        }

        return userToken.getUser();

    }
}
