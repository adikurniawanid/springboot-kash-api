package adi_kurniawan.springboot_kash_api.service;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.entity.UserDetail;
import adi_kurniawan.springboot_kash_api.entity.UserStatus;
import adi_kurniawan.springboot_kash_api.entity.UserToken;
import adi_kurniawan.springboot_kash_api.model.auth.*;
import adi_kurniawan.springboot_kash_api.repository.UserDetailRepository;
import adi_kurniawan.springboot_kash_api.repository.UserRepository;
import adi_kurniawan.springboot_kash_api.repository.UserStatusRepository;
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
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Transactional
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
    @Autowired
    private UserDetailRepository userDetailRepository;
    @Autowired
    private UserStatusRepository userStatusRepository;
    @Autowired
    private TokenService tokenService;


    private String getAlphaNumericString() {
        int length = 6;
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
        user.setPin("NOTSET");
        userRepository.save(user);

        UserDetail userDetail = new UserDetail();
        userDetail.setUser(user);
        userDetail.setName(request.getName());
        userDetailRepository.save(userDetail);

        UserStatus userStatus = new UserStatus();
        userStatus.setUser(user);
        userStatusRepository.save(userStatus);

        UserToken userToken = new UserToken();
        userToken.setUser(user);
        userTokenRepository.save(userToken);

        return AuthResponse.builder()
                .publicId(user.getPublicId())
                .name(userDetail.getName())
                .username(user.getUsername())
                .email(user.getEmail())
                .accessToken(user.getPublicId().toString())
                .build();
    }

    public AuthResponse login(LoginRequest request) {
        validationService.validate(request);

        User user = userRepository.findFirstByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Username or Password incorrect"));

        if (BCrypt.checkpw(request.getPassword() + user.getSalt() + pepper, user.getPassword())) {
            user.getUserToken().setAccessToken(
                    tokenService.generateToken(user)
            );
            userRepository.save(user);

            return AuthResponse.builder()
                    .publicId(user.getPublicId())
                    .name(user.getUserDetail().getName())
                    .username(user.getUsername())
                    .email(user.getEmail())
                    .accessToken(user.getUserToken().getAccessToken())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Incorrect password");
        }
    }

    public void requestVerification(RequestVerificationEmailRequest request) throws MessagingException, IOException {
        validationService.validate(request);
        User user = userRepository.findFirstByEmail(request.getEmail()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        if (Objects.nonNull(user.getUserStatus().getEmailVerifiedAt())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "User already verified");
        }

        String token = UUID.randomUUID().toString();
        String hashToken = BCrypt.hashpw(token, BCrypt.gensalt());
        String link = ServletUriComponentsBuilder.fromCurrentContextPath().build().toUriString() + "/api/auth/verify/" + token + "/" + user.getPublicId();

        user.getUserToken().setVerificationToken(hashToken);
        user.getUserToken().setVerificationTokenExpiredAt(System.currentTimeMillis() + (10000L * 60 * 60 * 24 * 30));
        userRepository.save(user);

        emailService.sendVerifyEmail(user, link);

        log.info("(DEV ONLY) VERIFICATION LINK : {}", link);
    }

    public void verification(String token, UUID publicId) throws MessagingException, IOException {
        User user = userRepository.findFirstByPublicId(publicId).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found")
        );

        if (user.getUserToken().getVerificationTokenExpiredAt() < System.currentTimeMillis()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired token, please request a new token");
        }

        if (Objects.nonNull(user.getUserToken().getVerificationToken()) && BCrypt.checkpw(token, user.getUserToken().getVerificationToken())) {
            user.getUserStatus().setEmailVerifiedAt(new Date());
            user.getUserToken().setVerificationToken(null);
            user.getUserToken().setVerificationTokenExpiredAt(null);
            userRepository.save(user);

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

        String token = getAlphaNumericString();
        String hashToken = BCrypt.hashpw(token, BCrypt.gensalt());

        user.getUserToken().setForgotPasswordToken(hashToken);
        user.getUserToken().setForgotPasswordTokenExpiredAt(System.currentTimeMillis() + (10000L * 60 * 60 * 24 * 30));
        userRepository.save(user);

        emailService.sendForgotPasswordEmail(user, token);

        log.info("(DEV ONLY) FORGOT PASSWORD TOKEN : {}", token);
    }


    public void changeForgotPassword(ChangeForgotPasswordRequest request) {
        validationService.validate(request);

        User user = userRepository.findFirstByEmail(request.getEmail()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Email not found")
        );

        if (Objects.nonNull(user.getUserToken().getForgotPasswordTokenExpiredAt()) && user.getUserToken().getForgotPasswordTokenExpiredAt() < System.currentTimeMillis()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Expired OTP, please request a new OTP");
        }

        if (BCrypt.checkpw(request.getOtp(), user.getUserToken().getForgotPasswordToken())) {
            String salt = BCrypt.gensalt();
            user.setSalt(salt);
            user.setPassword(BCrypt.hashpw(request.getNewPassword() + salt + pepper, BCrypt.gensalt()));
            user.getUserToken().setForgotPasswordToken(null);
            user.getUserToken().setForgotPasswordTokenExpiredAt(null);
            userRepository.save(user);
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid OTP");
        }
    }

    public void logout(User user) {
        user.getUserToken().setAccessToken(null);
    }
}
