package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.entity.UserStatus;
import adi_kurniawan.springboot_kash_api.model.User.ChangePasswordRequest;
import adi_kurniawan.springboot_kash_api.model.User.ChangePinRequest;
import adi_kurniawan.springboot_kash_api.model.User.OnboardingRequest;
import adi_kurniawan.springboot_kash_api.model.User.UserResponse;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.auth.AuthResponse;
import adi_kurniawan.springboot_kash_api.model.auth.LoginRequest;
import adi_kurniawan.springboot_kash_api.model.auth.RegisterRequest;
import adi_kurniawan.springboot_kash_api.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {
    private static final Logger log = LoggerFactory.getLogger(UserControllerTest.class);
    private final String userDummy_username = "adikurniawan";
    private final String userDummy_password = "adikurniawan";
    private final String userDummy_email = "adi@mail.com";
    private final String userDummy_name = "Adi Kurniawan";
    private final String userDummy_phone = "082108210821";
    private final String userDummy_avatarUrl = "adi.com/profilepicture";
    private final String userDummy_pin = "212121";
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private adi_kurniawan.springboot_kash_api.repository.UserStatusRepository UserStatusRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private UserDetailRepository userDetailRepository;
    @Autowired
    private PocketRepository pocketRepository;
    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private UserStatusRepository userStatusRepository;

    @BeforeEach
    void setUp() throws Exception {
        transactionRepository.deleteAll();
        pocketRepository.deleteAll();
        userTokenRepository.deleteAll();
        userDetailRepository.deleteAll();
        UserStatusRepository.deleteAll();
        userRepository.deleteAll();

        RegisterRequest registerRequest = new RegisterRequest();
        registerRequest.setUsername(userDummy_username);
        registerRequest.setPassword(userDummy_password);
        registerRequest.setEmail(userDummy_email);
        registerRequest.setName(userDummy_name);

        mockMvc.perform(
                post("/api/auth/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
        );

        User user = userRepository.findFirstByUsername(userDummy_username).orElse(null);

        assert user != null;
        UserStatus userStatus = userStatusRepository.findFirstByUserId(user.getId()).orElse(null);

        assert userStatus != null;
        userStatus.setEmailVerifiedAt(new Date());
        userStatusRepository.save(userStatus);
    }

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        pocketRepository.deleteAll();
        userTokenRepository.deleteAll();
        userDetailRepository.deleteAll();
        UserStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    WebResponse<AuthResponse> login() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(userDummy_username);
        loginRequest.setPassword(userDummy_password);

        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andReturn();

        WebResponse<AuthResponse> loginResponseJSON = objectMapper.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<>() {
        });

        return loginResponseJSON;
    }

    @Test
    void onboardingSuccess() throws Exception {
        WebResponse<AuthResponse> loginResponseJSON = login();

        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setPhone(userDummy_phone);
        onboardingRequest.setAvatarUrl(userDummy_avatarUrl);
        onboardingRequest.setPin(userDummy_pin);

        mockMvc.perform(
                post("/api/user/onboarding")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(onboardingRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully onboarding");

            User userDB = userRepository.findFirstByUsername(userDummy_username).orElse(null);
            assertNotNull(userDB);
            assertNotNull(userDB.getUserStatus().getOnboardedAt());
        });
    }

    @Test
    void getSuccess() throws Exception {
        onboardingSuccess();
        WebResponse<AuthResponse> loginResponseJSON = login();


        mockMvc.perform(MockMvcRequestBuilders.get("/api/user")
                .accept(MediaType.APPLICATION_JSON)
                .contentType(MediaType.APPLICATION_JSON)
                .header("Authorization", loginResponseJSON.getData().getAccessToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully retrieved user details");
            assertNotNull(response.getData());
            assertNotNull(response.getData().getPublicId());
            assertNotNull(response.getData().getUsername());
            assertNotNull(response.getData().getEmail());
            assertNotNull(response.getData().getName());
            assertNotNull(response.getData().getPhone());
            assertNotNull(response.getData().getAvatarUrl());

            User userDB = userRepository.findFirstByUsername(userDummy_username).orElse(null);
            assertNotNull(userDB);
            assertEquals(userDB.getPublicId(), response.getData().getPublicId());
            assertEquals(userDB.getUsername(), response.getData().getUsername());
            assertEquals(userDB.getEmail(), response.getData().getEmail());
            assertEquals(userDB.getUserDetail().getName(), response.getData().getName());
            assertEquals(userDB.getUserDetail().getPhone(), response.getData().getPhone());
            assertEquals(userDB.getUserDetail().getAvatarUrl(), response.getData().getAvatarUrl());
        });
    }

    @Test
    void changePassword() throws Exception {
        onboardingSuccess();
        WebResponse<AuthResponse> loginResponseJSON = login();

        ChangePasswordRequest changePasswordRequest = new ChangePasswordRequest();
        changePasswordRequest.setOldPassword(userDummy_password);
        changePasswordRequest.setNewPassword(userDummy_password + "new");


        mockMvc.perform(
                put("/api/user/change-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(changePasswordRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully change password");
        });
    }

    @Test
    void changePin() throws Exception {
        onboardingSuccess();
        WebResponse<AuthResponse> loginResponseJSON = login();

        ChangePinRequest changePinRequest = new ChangePinRequest();
        changePinRequest.setOldPin(userDummy_pin);
        changePinRequest.setNewPin("000000");

        mockMvc.perform(
                put("/api/user/change-pin")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(changePinRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<UserResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully change pin");
        });
    }
}