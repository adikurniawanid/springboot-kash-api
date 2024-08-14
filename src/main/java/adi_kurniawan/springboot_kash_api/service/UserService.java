package adi_kurniawan.springboot_kash_api.service;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.entity.UserDetail;
import adi_kurniawan.springboot_kash_api.model.User.ChangePinRequest;
import adi_kurniawan.springboot_kash_api.model.User.OnboardingRequest;
import adi_kurniawan.springboot_kash_api.model.User.UserResponse;
import adi_kurniawan.springboot_kash_api.repository.UserDetailRepository;
import adi_kurniawan.springboot_kash_api.repository.UserRepository;
import adi_kurniawan.springboot_kash_api.security.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class UserService {
    private static final Logger log = LoggerFactory.getLogger(UserService.class);
    @Autowired
    private UserDetailRepository userDetailRepository;
    @Autowired
    private ValidationService validationService;
    @Autowired
    private UserRepository userRepository;

    @Transactional
    public void onboarding(OnboardingRequest request) {
        validationService.validate(request);

        User user = userRepository.findFirstByPublicId(request.getPublicId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Unauthorized")
        );

        if (userDetailRepository.findFirstByUserId(user.getId()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Account already onboarding");
        } else if (userDetailRepository.findFirstByPhone(request.getPhone()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Phone already registered, please user another phone number");
        }

        UserDetail userDetail = new UserDetail();

        userDetail.setName(request.getName());
        userDetail.setPhone(request.getPhone());
        userDetail.setAvatarUrl(request.getAvatarUrl());

        userDetail.setUser(user);
        userDetailRepository.save(userDetail);

        user.setPin(BCrypt.hashpw(request.getPin(), BCrypt.gensalt()));
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

    public void changePin(User user, ChangePinRequest request) {
        if (BCrypt.checkpw(request.getOldPin(), user.getPin())) {
            user.setPin(BCrypt.hashpw(request.getNewPin(), BCrypt.gensalt()));
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong PIN");
        }
    }
}
