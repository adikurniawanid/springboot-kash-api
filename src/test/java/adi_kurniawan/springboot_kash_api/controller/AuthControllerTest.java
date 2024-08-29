package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.auth.AuthResponse;
import adi_kurniawan.springboot_kash_api.model.auth.LoginRequest;
import adi_kurniawan.springboot_kash_api.model.auth.RegisterRequest;
import adi_kurniawan.springboot_kash_api.model.auth.RequestForgotPasswordRequest;
import adi_kurniawan.springboot_kash_api.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    private final String userDummy_username = "adikurniawan";
    private final String userDummy_password = "adikurniawan";
    private final String userDummy_email = "adi@mail.com";
    private final String userDummy_name = "Adi Kurniawan";
    @Autowired
    private UserTokenRepository userTokenRepository;
    @Autowired
    private UserStatusRepository UserStatusRepository;
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

    @BeforeEach
    void setUp() {
        transactionRepository.deleteAll();
        pocketRepository.deleteAll();
        userTokenRepository.deleteAll();
        userDetailRepository.deleteAll();
        UserStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    @Test
    void registerSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(userDummy_username);
        request.setPassword(userDummy_password);
        request.setEmail(userDummy_email);
        request.setName(userDummy_name);

        mockMvc.perform(
                post("/api/auth/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
        ).andExpectAll(
                status().isOk()
        ).andDo(
                result -> {
                    WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getMessage());
                    assertEquals(response.getMessage(), "Register user successfully");
                    assertNotNull(response.getData());
                    assertNotNull(response.getData().getPublicId());
                    assertNotNull(response.getData().getName());
                    assertNotNull(response.getData().getUsername());
                    assertNotNull(response.getData().getEmail());
                    assertNotNull(response.getData().getAccessToken());

                    User userDB = userRepository.findFirstByUsername(userDummy_username).orElse(null);
                    assertNotNull(userDB);
                    assertEquals(userDB.getPublicId(), response.getData().getPublicId());
                    assertEquals(userDB.getUserDetail().getName(), response.getData().getName());
                    assertEquals(userDB.getUsername(), response.getData().getUsername());
                    assertEquals(userDB.getEmail(), response.getData().getEmail());
                    assertEquals(userDB.getUserToken().getAccessToken(), response.getData().getAccessToken());
                }
        );
    }

    @Test
    void loginSuccess() throws Exception {
        registerSuccess();

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(userDummy_username);
        loginRequest.setPassword(userDummy_password);

        mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(
                result -> {
                    WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getMessage());
                    assertEquals(response.getMessage(), "Login successfully");
                    assertNotNull(response.getData());
                    assertNotNull(response.getData().getPublicId());
                    assertNotNull(response.getData().getName());
                    assertNotNull(response.getData().getUsername());
                    assertNotNull(response.getData().getEmail());
                    assertNotNull(response.getData().getAccessToken());

                    User userDB = userRepository.findFirstByUsername(loginRequest.getUsername()).orElse(null);
                    assertNotNull(userDB);
                    assertEquals(userDB.getPublicId(), response.getData().getPublicId());
                    assertEquals(userDB.getUserDetail().getName(), response.getData().getName());
                    assertEquals(userDB.getUsername(), response.getData().getUsername());
                    assertEquals(userDB.getEmail(), response.getData().getEmail());
                    assertEquals(userDB.getUserToken().getAccessToken(), response.getData().getAccessToken());
                }
        );
    }

    @Test
    void logoutSuccess() throws Exception {
        registerSuccess();

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

        mockMvc.perform(
                post("/api/auth/logout")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Logout successfully");

            User userDB = userRepository.findFirstByUsername(loginRequest.getUsername()).orElse(null);
            assertNotNull(userDB);
            assertNull(userDB.getUserToken().getAccessToken());
        });
    }

    @Test
    void requestVerificationEmailSuccess() throws Exception {
        registerSuccess();

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

        mockMvc.perform(
                post("/api/auth/verify")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Request verification email successfully");

            User userDB = userRepository.findFirstByUsername(loginRequest.getUsername()).orElse(null);
            assertNotNull(userDB);
            assertNotNull(userDB.getUserToken().getVerificationToken());
        });
    }

    @Test
    void forgotPasswordSuccess() throws Exception {
        registerSuccess();

        RequestForgotPasswordRequest forgotPasswordRequest = new RequestForgotPasswordRequest();
        forgotPasswordRequest.setEmail(userDummy_email);

        mockMvc.perform(
                post("/api/auth/forgot-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(forgotPasswordRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(
                result -> {
                    WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
                    });
                    assertNotNull(response.getMessage());
                    assertEquals(response.getMessage(), "Please check your email inbox for One Time Password (OTP)");

                    User userDB = userRepository.findFirstByEmail(forgotPasswordRequest.getEmail()).orElse(null);
                    assertNotNull(userDB);
                    assertNotNull(userDB.getUserToken().getForgotPasswordToken());
                }
        );
    }


//    @Test
//    void verificationEmail() throws Exception {
//        requestVerificationEmail();
//
//        User user = userRepository.findFirstByUsername("adikurniawan").orElseThrow();
//
//        String url = "/api/auth/verify/" + user.getUserToken().getVerificationToken() + "/" + user.getPublicId();
//
//        log.info("URL >>> {}", url);
//
//        mockMvc.perform(
//                get(url)
//                        .accept(MediaType.APPLICATION_JSON)
//        ).andExpectAll(
//                status().isOk()
//        ).andDo(result -> {
//            WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
//            });
//            assertNotNull(response.getMessage());
//            assertEquals(response.getMessage(), "Verification email successfully");
//        });
//    }

    @Test
    void changeForgotPasswordSuccess() {
    }
}