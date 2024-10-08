package adi_kurniawan.springboot_kash_api.service;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.User.ChangePasswordRequest;
import adi_kurniawan.springboot_kash_api.model.User.ChangePinRequest;
import adi_kurniawan.springboot_kash_api.model.User.OnboardingRequest;
import adi_kurniawan.springboot_kash_api.model.User.UserResponse;
import adi_kurniawan.springboot_kash_api.repository.UserDetailRepository;
import adi_kurniawan.springboot_kash_api.repository.UserRepository;
import adi_kurniawan.springboot_kash_api.security.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.Date;
import java.util.Objects;

@Service
@Transactional
public class UserService {
    @Autowired
    private UserDetailRepository userDetailRepository;

    @Autowired
    private ValidationService validationService;

    @Autowired
    private UserRepository userRepository;

    @Value("${bcrypt.pepper}")
    private String pepper;


    public void onboarding(User user, OnboardingRequest request) {
        validationService.validate(request);

        if (Objects.nonNull(user.getUserStatus().getOnboardedAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already onboarding");
        }

        if (userDetailRepository.findFirstByPhone(request.getPhone()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone already registered, please user another phone number");
        }

        user.getUserDetail().setPhone(request.getPhone());
        user.getUserDetail().setAvatarUrl(request.getAvatarUrl());
        user.setPin(BCrypt.hashpw(request.getPin(), BCrypt.gensalt()));
        user.getUserStatus().setOnboardedAt(new Date());
        userRepository.save(user);
    }

    public UserResponse get(User user) {
        return UserResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .email(user.getEmail())
                .name(user.getUserDetail().getName())
                .phone(user.getUserDetail().getPhone())
                .avatarUrl(user.getUserDetail().getAvatarUrl())
                .build();
    }

    public void changePassword(User user, ChangePasswordRequest request) {
        validationService.validate(request);
        if (BCrypt.checkpw(request.getOldPassword() + user.getSalt() + pepper, user.getPassword())) {

            String salt = BCrypt.gensalt();
            user.setPassword(BCrypt.hashpw(request.getNewPassword() + salt + pepper, BCrypt.gensalt()));
            user.setSalt(salt);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }
    }

    public void changePin(User user, ChangePinRequest request) {
        validationService.validate(request);
        if (BCrypt.checkpw(request.getOldPin(), user.getPin())) {
            user.setPin(BCrypt.hashpw(request.getNewPin(), BCrypt.gensalt()));
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong PIN");
        }
    }
}
