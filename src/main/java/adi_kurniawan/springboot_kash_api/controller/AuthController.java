package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.auth.AuthResponse;
import adi_kurniawan.springboot_kash_api.model.auth.LoginRequest;
import adi_kurniawan.springboot_kash_api.model.auth.RegisterRequest;
import adi_kurniawan.springboot_kash_api.model.auth.RequestVerificationEmailRequest;
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

    @PostMapping(path = "/api/auth/verify",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse requestVerificationEmail(@RequestBody RequestVerificationEmailRequest request) throws MessagingException, IOException {
        authService.requestVerification(request);
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
}
