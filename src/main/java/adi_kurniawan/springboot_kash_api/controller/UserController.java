package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.User.OnboardingRequest;
import adi_kurniawan.springboot_kash_api.model.User.UserResponse;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping(
            path = "/api/user/{userPublicId}/onboarding",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse onboarding(@PathVariable("userPublicId") UUID userPublicId,
                                  @RequestBody OnboardingRequest request) {

        request.setPublicId(userPublicId);

        userService.onboarding(request);

        return WebResponse
                .<UserResponse>builder()
                .message("Successfully onboarding")
                .build();
    }

    @GetMapping(path = "/api/user",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<UserResponse> get(User user) {
        UserResponse userResponse = userService.get(user);
        return WebResponse.
                <UserResponse>builder()
                .message("Successfully retrieved user details")
                .data(userResponse)
                .build();
    }
}
