package adi_kurniawan.springboot_kash_api.service;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.entity.UserToken;
import adi_kurniawan.springboot_kash_api.model.auth.*;
import adi_kurniawan.springboot_kash_api.repository.UserRepository;
import adi_kurniawan.springboot_kash_api.repository.UserTokenRepository;
import adi_kurniawan.springboot_kash_api.security.BCrypt;
import jakarta.mail.MessagingException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    @Autowired
    private ValidationService validationService;

    @Autowired
    private UserRepository userRepository;

    @Value("${bcrypt.pepper}")
    private String pepper;

    @Autowired
    private EmailService emailService;

    @Autowired
    private UserTokenRepository userTokenRepository;

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validationService.validate(request);

        if (userRepository.findFirstByEmail(request.getEmail()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already registered");
        }

        if (userRepository.findFirstByUsername(request.getUsername()).isPresent()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already registered");
        }

        String salt = BCrypt.gensalt();

        User user = new User();
        user.setPublicId(UUID.randomUUID());
        user.setSalt(salt);
        user.setEmail(request.getEmail());
        user.setPassword(BCrypt.hashpw(request.getPassword() + salt + pepper, BCrypt.gensalt()));
        user.setUsername(request.getUsername());
        user.setIsVerified(false);
        user.setPin("NOTSET");

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


        if (BCrypt.checkpw(request.getPassword() + user.getSalt() + pepper, user.getPassword())) {
            return AuthResponse.builder()
                    .publicId(user.getPublicId())
                    .username(user.getUsername())
                    .accessToken(user.getPublicId().toString())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }
    }

    public void requestVerification(RequestVerificationEmailRequest request) throws MessagingException, IOException {
        validationService.validate(request);
        User user = userRepository.findFirstByUsername(request.getUsername()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        if (user.getIsVerified()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already verified");
        }

        String token = UUID.randomUUID().toString();

        String hashToken = BCrypt.hashpw(token, BCrypt.gensalt());

        String link = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/api/auth/verify/" + token + "/" + user.getPublicId();


        UserToken existUser = userTokenRepository.findFirstByUserId(user.getId()).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "token invalid"));
        existUser.setVerificationToken(hashToken);
        existUser.setVerificationTokenExpireedAt(System.currentTimeMillis() + (10000 * 60 * 60 * 24 * 30));
        userTokenRepository.save(existUser);


        log.info("(DEV ONLY) VERIFICATION LINK : {}", link);

        emailService.sendVerifyEmail(user, link);
    }


    public void verification(String token, UUID publicId) throws MessagingException, IOException {

        User user = userRepository.findFirstByPublicId(publicId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token")
        );

        UserToken userToken = userTokenRepository.findFirstByUserId(user.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token")
        );

        if (Objects.isNull(userToken.getVerificationTokenExpireedAt())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid token");
        }

        if (userToken.getVerificationTokenExpireedAt() < System.currentTimeMillis()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired token, please request a new token");
        }

        if (BCrypt.checkpw(token, userToken.getVerificationToken())) {
            user.setIsVerified(true);
            userRepository.save(user);

            userToken.setVerificationToken(null);
            userToken.setVerificationTokenExpireedAt(null);
            userTokenRepository.save(userToken);

            emailService.sendWelcomeEmail(user);


        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid token");
        }


    }

    public void forgotPassword(RequestForgotPasswordRequest request) throws MessagingException, IOException {
        validationService.validate(request);

        User user = userRepository.findFirstByEmail(request.getEmail()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found")
        );

        UserToken userToken = userTokenRepository.findFirstByUserId(user.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found")
        );

        String token = getAlphaNumericString();

        String hashToken = BCrypt.hashpw(token, BCrypt.gensalt());

        userToken.setUser(user);
        userToken.setForgotPasswordToken(hashToken);
        userToken.setForgotPasswordTokenExpireedAt(System.currentTimeMillis() + (10000 * 60 * 60 * 24 * 30));
        userTokenRepository.save(userToken);

        emailService.sendForgotPasswordEmail(user, token);

        log.info("(DEV ONLY) FORGOT PASSWORD TOKEN : {}", token);

    }

    private String getAlphaNumericString() {
        Integer length = 6;
        String AlphaNumericString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"
                + "0123456789"
                + "abcdefghijklmnopqrstuvxyz";

        StringBuilder sb = new StringBuilder(length);

        for (int i = 0; i < length; i++) {

            int index
                    = (int) (AlphaNumericString.length()
                    * Math.random());

            sb.append(AlphaNumericString
                    .charAt(index));
        }

        return sb.toString();

    }

    public void changeForgotPassword(ChangeForgotPasswordRequest request) {
        validationService.validate(request);

        User user = userRepository.findFirstByEmail(request.getEmail()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found")
        );

        UserToken userToken = userTokenRepository.findFirstByUserId(user.getId()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid OTP")
        );

        if (Objects.isNull(userToken.getForgotPasswordTokenExpireedAt())) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Invalid OTP");
        }

        if (userToken.getForgotPasswordTokenExpireedAt() < System.currentTimeMillis()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired OTP, please request a new OTP");
        }

        if (BCrypt.checkpw(request.getOtp(), userToken.getForgotPasswordToken())) {
            String salt = BCrypt.gensalt();
            user.setSalt(salt);
            user.setPassword(BCrypt.hashpw(request.getNewPassword() + salt + pepper, BCrypt.gensalt()));
            userToken.setForgotPasswordToken(null);
            userToken.setForgotPasswordTokenExpireedAt(null);
            userTokenRepository.save(userToken);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }


    }
}
