package adi_kurniawan.springboot_kash_api.service;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.auth.AuthResponse;
import adi_kurniawan.springboot_kash_api.model.auth.LoginRequest;
import adi_kurniawan.springboot_kash_api.model.auth.RegisterRequest;
import adi_kurniawan.springboot_kash_api.repository.UserRepository;
import adi_kurniawan.springboot_kash_api.security.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    @Autowired
    private ValidationService validationService;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validationService.validate(request);

        if (userRepository.findFirstByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        if (userRepository.findFirstByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already registered");
        }

        User user = new User();
        user.setPublicId(UUID.randomUUID());
        user.setEmail(request.getEmail());
        user.setPassword(BCrypt.hashpw(request.getPassword(), BCrypt.gensalt()));
        user.setUsername(request.getUsername());

        userRepository.save(user);

        return AuthResponse.builder()
                .publicId(user.getPublicId())
                .username(user.getUsername())
                .accessToken(user.getPublicId().toString())
                .build();
    }

    @Transactional
    public AuthResponse login(LoginRequest request) {
        validationService.validate(request);

        User user = userRepository.findFirstByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password incorrect"));

        if (BCrypt.checkpw(request.getPassword(), user.getPassword())) {
            return AuthResponse.builder()
                    .publicId(user.getPublicId())
                    .username(user.getUsername())
                    .accessToken(user.getPublicId().toString())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }

    }
}
