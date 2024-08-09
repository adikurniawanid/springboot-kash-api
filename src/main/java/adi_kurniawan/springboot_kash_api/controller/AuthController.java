package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.auth.AuthResponse;
import adi_kurniawan.springboot_kash_api.model.auth.LoginRequest;
import adi_kurniawan.springboot_kash_api.model.auth.RegisterRequest;
import adi_kurniawan.springboot_kash_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthController {
    @Autowired
    private AuthService authService;

    @PostMapping(
            path = "/api/register",
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

    @PostMapping(path = "/api/login",
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
}
