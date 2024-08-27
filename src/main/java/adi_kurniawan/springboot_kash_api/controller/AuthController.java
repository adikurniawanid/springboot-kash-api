package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.auth.*;
import adi_kurniawan.springboot_kash_api.service.AuthService;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.UUID;

@RestController
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping(
            path = "/api/auth/register",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<AuthResponse> register(@RequestBody RegisterRequest request) {
        AuthResponse registeredUser = authService.register(request);
        return WebResponse
                .<AuthResponse>builder()
                .message("Register user successfully")
                .data(registeredUser)
                .build();
    }

    @PostMapping(path = "/api/auth/login",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<AuthResponse> login(@RequestBody LoginRequest request) {
        AuthResponse loginUser = authService.login(request);
        return WebResponse
                .<AuthResponse>builder()
                .message("Login successfully")
                .data(loginUser)
                .build();
    }

    @PostMapping(path = "/api/auth/logout",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<AuthResponse> logout(User user) {
        authService.logout(user);
        return WebResponse
                .<AuthResponse>builder()
                .message("Logout successfully")
                .build();
    }

    @PostMapping(path = "/api/auth/verify",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse requestVerificationEmail(User user) throws MessagingException, IOException {
        authService.requestVerification(user);
        return WebResponse
                .builder()
                .message("Request verification email successfully")
                .build();
    }

    @GetMapping(path = "/api/auth/verify/{token}/{publicId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse verificationEmail(@PathVariable("token") String token,
                                         @PathVariable("publicId") UUID publicId) throws MessagingException, IOException {
        authService.verification(token, publicId);
        return WebResponse
                .builder()
                .message("Verification email successfully")
                .build();
    }

    @PostMapping(path = "/api/auth/forgot-password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse forgotPassword(@RequestBody RequestForgotPasswordRequest request) throws MessagingException, IOException {
        authService.forgotPassword(request);
        return WebResponse
                .builder()
                .message("Please check your email inbox for One Time Password (OTP)")
                .build();
    }

    @PutMapping(path = "/api/auth/forgot-password",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse changeForgotPassword(@RequestBody ChangeForgotPasswordRequest request) {
        authService.changeForgotPassword(request);
        return WebResponse
                .builder()
                .message("Change password successfully")
                .build();
    }
}
