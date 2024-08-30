package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.Pocket;
import adi_kurniawan.springboot_kash_api.entity.Transaction;
import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.entity.UserStatus;
import adi_kurniawan.springboot_kash_api.model.User.OnboardingRequest;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.auth.AuthResponse;
import adi_kurniawan.springboot_kash_api.model.auth.RegisterRequest;
import adi_kurniawan.springboot_kash_api.model.pocket.CreatePocketRequest;
import adi_kurniawan.springboot_kash_api.model.pocket.PocketResponse;
import adi_kurniawan.springboot_kash_api.model.transaction.*;
import adi_kurniawan.springboot_kash_api.repository.*;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class TransactionControllerTest {
    private static final Logger log = LoggerFactory.getLogger(TransactionControllerTest.class);
    private final String userDummy_username = "adikurniawan";
    private final String userDummy_password = "adikurniawan";
    private final String userDummy_email = "adi@mail.com";
    private final String userDummy_name = "Adi Kurniawan";
    private final String userDummy_phone = "082108210821";
    private final String userDummy_avatarUrl = "adi.com/profilepicture";
    private final String userDummy_pin = "212121";
    WebResponse<AuthResponse> registerResponseJSON = new WebResponse<>();
    WebResponse<AuthResponse> registerResponseJSON2 = new WebResponse<>();
    WebResponse<PocketResponse> createPocketResponseJSON = new WebResponse<>();
    WebResponse<PocketResponse> createPocketResponseJSON2 = new WebResponse<>();

    Transaction transactionResultJSON = new Transaction();

    private String generatedCodePay = "";
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

        RegisterRequest registerRequest2 = new RegisterRequest();
        registerRequest2.setUsername(userDummy_username + "2");
        registerRequest2.setPassword(userDummy_password + "2");
        registerRequest2.setEmail(userDummy_email + "2");
        registerRequest2.setName(userDummy_name + "2");

        MvcResult registerResult = mockMvc.perform(
                post("/api/auth/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest))
        ).andReturn();

        MvcResult registerResult2 = mockMvc.perform(
                post("/api/auth/register")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest2))
        ).andReturn();

        registerResponseJSON = objectMapper.readValue(registerResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        registerResponseJSON2 = objectMapper.readValue(registerResult2.getResponse().getContentAsString(), new TypeReference<>() {
        });

        User user = userRepository.findFirstByUsername(userDummy_username).orElse(null);
        User user2 = userRepository.findFirstByUsername(userDummy_username + "2").orElse(null);

        assert user != null && user2 != null;
        UserStatus userStatus = userStatusRepository.findFirstByUserId(user.getId()).orElse(null);
        UserStatus userStatus2 = userStatusRepository.findFirstByUserId(user2.getId()).orElse(null);

        assert userStatus != null && userStatus2 != null;
        userStatus.setEmailVerifiedAt(new Date());
        userStatusRepository.save(userStatus);
        userStatus2.setEmailVerifiedAt(new Date());
        userStatusRepository.save(userStatus2);

        OnboardingRequest onboardingRequest = new OnboardingRequest();
        onboardingRequest.setPhone(userDummy_phone);
        onboardingRequest.setAvatarUrl(userDummy_avatarUrl);
        onboardingRequest.setPin(userDummy_pin);

        OnboardingRequest onboardingRequest2 = new OnboardingRequest();
        onboardingRequest2.setPhone(userDummy_phone + "2");
        onboardingRequest2.setAvatarUrl(userDummy_avatarUrl + "2");
        onboardingRequest2.setPin(userDummy_pin.replaceAll("2", "1"));

        mockMvc.perform(
                post("/api/user/onboarding")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(onboardingRequest))
        );

        mockMvc.perform(
                post("/api/user/onboarding")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON2.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(onboardingRequest2))
        );

        CreatePocketRequest createPocketRequest = new CreatePocketRequest("Kantong Makan");
        CreatePocketRequest createPocketRequest2 = new CreatePocketRequest("Kantong Transportasi");

        MvcResult createPocketResult = mockMvc.perform(
                post("/api/pocket")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(createPocketRequest))
        ).andReturn();

        MvcResult createPocketResult2 = mockMvc.perform(
                post("/api/pocket")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON2.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(createPocketRequest2))
        ).andReturn();

        createPocketResponseJSON = objectMapper.readValue(createPocketResult.getResponse().getContentAsString(), new TypeReference<>() {
        });
        createPocketResponseJSON2 = objectMapper.readValue(createPocketResult2.getResponse().getContentAsString(), new TypeReference<>() {
        });
    }

//    @AfterEach
//    void tearDown() {
//        transactionRepository.deleteAll();
//        pocketRepository.deleteAll();
//        userTokenRepository.deleteAll();
//        userDetailRepository.deleteAll();
//        UserStatusRepository.deleteAll();
//        userRepository.deleteAll();
//    }

    @Test
    void inquirySuccess() throws Exception {
        mockMvc.perform(
                MockMvcRequestBuilders.get("/api/transaction/inquiry/" + createPocketResponseJSON2.getData().getAccountNumber())
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON.getData().getAccessToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<InquiryResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully inquiry account");
            assertNotNull(response.getData());
            assertEquals(response.getData().getName(), registerResponseJSON2.getData().getName());

            Pocket pocketDB = pocketRepository.findFirstByAccountNumber(createPocketResponseJSON2.getData().getAccountNumber()).orElse(null);
            assertNotNull(pocketDB);
            assertEquals(pocketDB.getName(), createPocketResponseJSON2.getData().getName());
        });
    }

    @Test
    void transferSuccess() throws Exception {
        topUpSuccess();
        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(createPocketResponseJSON.getData().getAccountNumber());
        transferRequest.setDestinationAccountNumber(createPocketResponseJSON2.getData().getAccountNumber());
        transferRequest.setAmount(5000L);
        transferRequest.setDescription("INTEGRATION TESTING - TRANSFER REQUEST");
        transferRequest.setPin(userDummy_pin);

        mockMvc.perform(
                post("/api/transaction/transfer")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(transferRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<TransferResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully transfer");
            assertNotNull(response.getData());
            assertNotNull(response.getData().getTimestamp());
            assertNotNull(response.getData().getJournalNumber());
            assertEquals(response.getData().getSenderName(), registerResponseJSON.getData().getName());
            assertEquals(response.getData().getSourceAccountNumber(), createPocketResponseJSON.getData().getAccountNumber());
            assertEquals(response.getData().getAmount(), transferRequest.getAmount());
            assertEquals(response.getData().getReceiverName(), registerResponseJSON2.getData().getName());
            assertEquals(response.getData().getDestinationAccountNumber(), createPocketResponseJSON2.getData().getAccountNumber());
            assertEquals(response.getData().getDescription(), transferRequest.getDescription());


            Pocket pocket = pocketRepository.findFirstByAccountNumber(createPocketResponseJSON.getData().getAccountNumber()).orElse(null);
            assertNotNull(pocket);
            assertEquals(pocket.getBalance(), 1000000L - transferRequest.getAmount());

            Pocket pocket2 = pocketRepository.findFirstByAccountNumber(createPocketResponseJSON2.getData().getAccountNumber()).orElse(null);
            assertNotNull(pocket2);
            assertEquals(pocket2.getBalance(), 1000000L + transferRequest.getAmount());

            List<Transaction> transaction = transactionRepository.findAllBySourceAccountNumberOrDestinationAccountNumber(createPocketResponseJSON.getData().getAccountNumber(), createPocketResponseJSON2.getData().getAccountNumber());
            assertNotNull(transaction);

            transactionResultJSON = transaction.get(2);
        });

    }

    @Test
    void topUpSuccess() throws Exception {
        TopUpRequest topUpRequest = new TopUpRequest();
        topUpRequest.setAccountNumber(createPocketResponseJSON.getData().getAccountNumber());
        topUpRequest.setAmount(1000000L);

        mockMvc.perform(
                post("/api/transaction/top-up")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(topUpRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<TransferResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully top up pocket");

            Pocket pocket = pocketRepository.findFirstByAccountNumber(createPocketResponseJSON.getData().getAccountNumber()).orElse(null);
            assertNotNull(pocket);
            assertEquals(pocket.getBalance(), topUpRequest.getAmount());

            List<Transaction> transaction = transactionRepository.findAllBySourceAccountNumberOrDestinationAccountNumber(createPocketResponseJSON.getData().getAccountNumber(), createPocketResponseJSON.getData().getAccountNumber());
            assertNotNull(transaction);
        });

        TopUpRequest topUpRequest2 = new TopUpRequest();
        topUpRequest2.setAccountNumber(createPocketResponseJSON2.getData().getAccountNumber());
        topUpRequest2.setAmount(1000000L);

        mockMvc.perform(
                post("/api/transaction/top-up")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON2.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(topUpRequest2))
        );
    }

    @Test
    void createCodePaySuccess() throws Exception {
        CreateCodePayRequest createCodePayRequest = new CreateCodePayRequest();
        createCodePayRequest.setDestinationAccountNumber(createPocketResponseJSON2.getData().getAccountNumber());
        createCodePayRequest.setAmount(5000L);
        createCodePayRequest.setDescription("INTEGRATION TESTING - GENERATE CODE PAY");
        createCodePayRequest.setExpiredAt(new Date(new Date().getTime() + 86400000));

        mockMvc.perform(
                post("/api/transaction/code-pay")
                        .accept(MediaType.APPLICATION_JSON)
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON2.getData().getAccessToken())
                        .content(objectMapper.writeValueAsString(createCodePayRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<CreateCodePayResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully create code pay");
            assertNotNull(response.getData());
            assertNotNull(response.getData().getCode());

            generatedCodePay = response.getData().getCode();
        });
    }

    @Test
    void getCodePaySuccess() throws Exception {
        createCodePaySuccess();

        mockMvc.perform(
                get("/api/transaction/code-pay/" + generatedCodePay)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON.getData().getAccessToken())
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<CodePayResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully get information code pay");
            assertNotNull(response.getData());
            assertNotNull(response.getData().getDestinationAccountNumber());
            assertEquals(response.getData().getDestinationAccountNumber(), createPocketResponseJSON2.getData().getAccountNumber());
            assertEquals(response.getData().getAmount(), 5000L);
            assertEquals(response.getData().getDescription(), "INTEGRATION TESTING - GENERATE CODE PAY");
            assertNotNull(response.getData().getExpiredAt());
        });
    }

    @Test
    void codePayPaymentSuccess() throws Exception {
        topUpSuccess();
        createCodePaySuccess();

        CodePayRequest codePayRequest = new CodePayRequest();
        codePayRequest.setAccountNumber(createPocketResponseJSON.getData().getAccountNumber());
        codePayRequest.setPin(userDummy_pin);

        mockMvc.perform(
                post("/api/transaction/code-pay/" + generatedCodePay)
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON.getData().getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(codePayRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<TransferResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully transfer via code pay");
            assertNotNull(response.getData());
            assertNotNull(response.getData().getTimestamp());
            assertNotNull(response.getData().getJournalNumber());
            assertEquals(response.getData().getSenderName(), registerResponseJSON.getData().getName());
            assertEquals(response.getData().getSourceAccountNumber(), createPocketResponseJSON.getData().getAccountNumber());
            assertEquals(response.getData().getAmount(), 5000L);
            assertEquals(response.getData().getReceiverName(), registerResponseJSON2.getData().getName());
            assertEquals(response.getData().getDestinationAccountNumber(), createPocketResponseJSON2.getData().getAccountNumber());
            assertEquals(response.getData().getDescription(), "CODEPAY INTEGRATION TESTING - GENERATE CODE PAY");


            Pocket pocket = pocketRepository.findFirstByAccountNumber(createPocketResponseJSON.getData().getAccountNumber()).orElse(null);
            assertNotNull(pocket);
            assertEquals(pocket.getBalance(), 1000000L - 5000L);

            Pocket pocket2 = pocketRepository.findFirstByAccountNumber(createPocketResponseJSON2.getData().getAccountNumber()).orElse(null);
            assertNotNull(pocket2);
            assertEquals(pocket2.getBalance(), 1000000L + 5000L);

            List<Transaction> transaction = transactionRepository.findAllBySourceAccountNumberOrDestinationAccountNumber(createPocketResponseJSON.getData().getAccountNumber(), createPocketResponseJSON2.getData().getAccountNumber());
            assertNotNull(transaction);
        });
    }

    @Test
    void reversalSuccess() throws Exception {
        transferSuccess();

        ReversalRequest reversalRequest = new ReversalRequest();
        reversalRequest.setReason("DANA TIDAK MASUK KE AKUN TUJUAN");
        reversalRequest.setPin(userDummy_pin.replaceAll("2", "1"));

        mockMvc.perform(
                post("/api/transaction/" + transactionResultJSON.getJournalNumber() + "/reversal")
                        .accept(MediaType.APPLICATION_JSON)
                        .header("Authorization", registerResponseJSON2.getData().getAccessToken())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(reversalRequest))
        ).andExpectAll(
                status().isOk()
        ).andDo(result -> {
            WebResponse<TransferResponse> response = objectMapper.readValue(result.getResponse().getContentAsString(), new TypeReference<>() {
            });
            assertNotNull(response.getMessage());
            assertEquals(response.getMessage(), "Successfully reversal transaction");

            Pocket pocket = pocketRepository.findFirstByAccountNumber(createPocketResponseJSON2.getData().getAccountNumber()).orElse(null);
            assertNotNull(pocket);
            assertEquals(1000000L, pocket.getBalance());

            Pocket pocket2 = pocketRepository.findFirstByAccountNumber(createPocketResponseJSON.getData().getAccountNumber()).orElse(null);
            assertNotNull(pocket2);
            assertEquals(1000000L, pocket2.getBalance());

            List<Transaction> transaction = transactionRepository.findAllBySourceAccountNumberOrDestinationAccountNumber(createPocketResponseJSON.getData().getAccountNumber(), createPocketResponseJSON2.getData().getAccountNumber());
            assertNotNull(transaction);
        });
    }
}