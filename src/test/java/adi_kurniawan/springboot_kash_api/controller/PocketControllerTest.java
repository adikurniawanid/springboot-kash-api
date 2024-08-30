package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.Pocket;
import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.entity.UserStatus;
import adi_kurniawan.springboot_kash_api.model.User.OnboardingRequest;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.auth.AuthResponse;
import adi_kurniawan.springboot_kash_api.model.auth.RegisterRequest;
import adi_kurniawan.springboot_kash_api.model.pocket.CreatePocketRequest;
import adi_kurniawan.springboot_kash_api.model.pocket.PocketResponse;
import adi_kurniawan.springboot_kash_api.model.pocket.RenamePocketRequest;
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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class PocketControllerTest {
    private static final Logger log = LoggerFactory.getLogger(PocketControllerTest.class);
    private final String userDummy_username = "adikurniawan";
    private final String userDummy_password = "adikurniawan";
    private final String userDummy_email = "adi@mail.com";
    private final String userDummy_name = "Adi Kurniawan";
    private final String userDummy_phone = "082108210821";
    private final String userDummy_avatarUrl = "adi.com/profilepicture";
    private final String userDummy_pin = "212121";
    WebResponse<PocketResponse> pocketResponseJSON = new WebResponse<>();
    WebResponse<AuthResponse> loginResponseJSON = new WebResponse<>();
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

        MvcResult registerResult = mockMvc.perform(
                post("/api/auth/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
        ).andReturn();

        WebResponse<AuthResponse> registerResponseJSON = objectMapper.readValue(registerResult.getResponse().getContentAsString(), new TypeReference<>() {
        });

        User user = userRepository.findFirstByUsername(userDummy_username).orElse(null);

        assert user != null;
        UserStatus userStatus = userStatusRepository.findFirstByUserId(user.getId()).orElse(null);

        assert userStatus != null;
        userStatus.setEmailVerifiedAt(new Date());
        userStatusRepository.save(userStatus);

        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setPhone(userDummy_phone);
        onboardingRequest.setAvatarUrl(userDummy_avatarUrl);
        onboardingRequest.setPin(userDummy_pin);

        mockMvc.perform(
                post("/api/user/onboarding")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(onboardingRequest))
        );

        loginResponseJSON = registerResponseJSON;
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

    @Test
    void createSuccess() throws Exception {
        CreatePocketRequest createPocketRequest = new CreatePocketRequest();
        createPocketRequest.setName("Kantong Makan");

        mockMvc.perform(
                post("/api/pocket")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(createPocketRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<PocketResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Pocket created successfully");
            assertNotNull(response.getData());
            assertNotNull(response.getData().getAccountNumber());
            assertNotNull(response.getData().getName());
            assertNotNull(response.getData().getBalance());

            Pocket pocketDB = pocketRepository.findFirstByAccountNumber(response.getData().getAccountNumber()).orElse(null);

            assertNotNull(pocketDB);
            assertEquals(pocketDB.getAccountNumber(), response.getData().getAccountNumber());
            assertEquals(pocketDB.getName(), response.getData().getName());
            assertEquals(pocketDB.getBalance(), response.getData().getBalance());

            pocketResponseJSON = response;
        });
    }

    @Test
    void listSuccess() throws Exception {
        createSuccess();
        createSuccess();

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/pocket")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<PocketResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully retrieved list of pockets");
            assertEquals(2, response.getData().size());
        });
    }

    @Test
    void get() throws Exception {
        createSuccess();

        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/pocket/" + pocketResponseJSON.getData().getAccountNumber())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<PocketResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Get pocket detail successfully");
            assertNotNull(response.getData());
            assertNotNull(response.getData().getAccountNumber());
            assertNotNull(response.getData().getName());
            assertNotNull(response.getData().getBalance());

            Pocket pocket = pocketRepository.findFirstByAccountNumber(pocketResponseJSON.getData().getAccountNumber()).orElse(null);

            assertNotNull(pocket);
            assertEquals(response.getData().getAccountNumber(), pocket.getAccountNumber());
            assertEquals(response.getData().getName(), pocket.getName());
            assertEquals(response.getData().getBalance(), pocket.getBalance());
        });
    }


    @Test
    void history() throws Exception {
        createSuccess();
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/pocket/" + pocketResponseJSON.getData().getAccountNumber() + "/history")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<List<PocketResponse>> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Get history list successfully");
        });
    }

    @Test
    void rename() throws Exception {
        createSuccess();
        RenamePocketRequest renamePocketRequest = new RenamePocketRequest();
        renamePocketRequest.setName("NEW POCKET");

        mockMvc.perform(
                MockMvcRequestBuilders.put("/api/pocket/" + pocketResponseJSON.getData().getAccountNumber())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", loginResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(renamePocketRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<PocketResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Rename pocket successfully");

            Pocket pocket = pocketRepository.findFirstByAccountNumber(pocketResponseJSON.getData().getAccountNumber()).orElse(null);

            assertNotNull(pocket);
            assertEquals("NEW POCKET", pocket.getName());
        });
    }
}