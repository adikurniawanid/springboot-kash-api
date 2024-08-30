package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.entity.UserToken;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.auth.*;
import adi_kurniawan.springboot_kash_api.repository.*;
import adi_kurniawan.springboot_kash_api.security.BCrypt;
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

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerTest {
    private static final Logger log = LoggerFactory.getLogger(AuthControllerTest.class);
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

    @AfterEach
    void tearDown() {
        transactionRepository.deleteAll();
        pocketRepository.deleteAll();
        userTokenRepository.deleteAll();
        userDetailRepository.deleteAll();
        UserStatusRepository.deleteAll();
        userRepository.deleteAll();
    }

    public WebResponse<AuthResponse> login() throws Exception {
        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername(userDummy_username);
        loginRequest.setPassword(userDummy_password);

        MvcResult loginResult = mockMvc.perform(
                post("/api/auth/login")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest))
        ).andReturn();
        return objectMapper.readValue(loginResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
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

        WebResponse<AuthResponse> loginResponseJSON = login();

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

            User userDB = userRepository.findFirstByUsername(userDummy_username).orElse(null);
            assertNotNull(userDB);
            assertNull(userDB.getUserToken().getAccessToken());
        });
    }

    @Test
    void requestVerificationEmailSuccess() throws Exception {
        registerSuccess();
        WebResponse<AuthResponse> loginResponseJSON = login();

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

            User userDB = userRepository.findFirstByUsername(userDummy_username).orElse(null);
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


    @Test
    void verificationEmailSuccess() throws Exception {
        requestVerificationEmailSuccess();

        User user = userRepository.findFirstByUsername(userDummy_username).orElse(null);

        String token = UUID.randomUUID().toString();
        String hashToken = BCrypt.hashpw(token, BCrypt.gensalt());

        assert user != null;
        UserToken userToken = userTokenRepository.findFirstByUserId(user.getId()).orElse(null);

        assert userToken != null;
        userToken.setVerificationToken(hashToken);
        userTokenRepository.save(userToken);

        mockMvc.perform(
                get("/api/auth/verify/" + token + "/" + user.getPublicId())
                        .accept(MediaType.APPLICATION_JSON)
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Verification email successfully");
        });
    }

    @Test
    void changeForgotPasswordSuccess() throws Exception {
        forgotPasswordSuccess();

        ChangeForgotPasswordRequest changeForgotPasswordRequest = new ChangeForgotPasswordRequest();
        changeForgotPasswordRequest.setNewPassword(userDummy_password);
        changeForgotPasswordRequest.setEmail(userDummy_email);

        User user = userRepository.findFirstByUsername(userDummy_username).orElse(null);

        String token = "ABCDEF";
        String hashToken = BCrypt.hashpw(token, BCrypt.gensalt());
        changeForgotPasswordRequest.setOtp(token);

        assert user != null;
        UserToken userToken = userTokenRepository.findFirstByUserId(user.getId()).orElse(null);

        assert userToken != null;
        userToken.setForgotPasswordToken(hashToken);
        userTokenRepository.save(userToken);

        mockMvc.perform(
                put("/api/auth/forgot-password")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeForgotPasswordRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<AuthResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Change password successfully");
        });
    }
}