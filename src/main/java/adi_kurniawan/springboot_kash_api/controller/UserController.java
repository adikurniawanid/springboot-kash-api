package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.User.ChangePasswordRequest;
import adi_kurniawan.springboot_kash_api.model.User.ChangePinRequest;
import adi_kurniawan.springboot_kash_api.model.User.OnboardingRequest;
import adi_kurniawan.springboot_kash_api.model.User.UserResponse;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@RestController
public class UserController {

    @Autowired
    private UserService userService;


    @PostMapping(
            path = "/api/user/onboarding",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse onboarding(User user,
                                  @RequestBody OnboardingRequest request) {

        userService.onboarding(user, request);

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

    @PutMapping(
            path = "/api/user/change-password",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse changePassword(User user,
                                      @RequestBody ChangePasswordRequest request) {

        userService.changePassword(user, request);

        return WebResponse
                .builder()
                .message("Successfully change password")
                .build();
    }

    @PutMapping(
            path = "/api/user/change-pin",
            consumes = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse changePin(User user,
                                 @RequestBody ChangePinRequest request) {

        userService.changePin(user, request);

        return WebResponse
                .builder()
                .message("Successfully change pin")
                .build();
    }
}
