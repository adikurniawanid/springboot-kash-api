package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.transaction.*;
import adi_kurniawan.springboot_kash_api.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;
import java.util.UUID;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping(
            path = "api/transaction/inquiry/{accountNumber}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<InquiryResponse> inquiry(User user,
                                                @PathVariable("accountNumber") BigInteger accountNumber) {
        InquiryResponse inquiredUser = transactionService.inquiry(user, accountNumber);

        return WebResponse
                .<InquiryResponse>builder()
                .message("Successfully inquiry account")
                .data(inquiredUser)
                .build();
    }

    @PostMapping(
            path = "/api/transaction/transfer",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<TransferResponse> transfer(User user,
                                                  @RequestBody TransferRequest request) {
        TransferResponse transferResponse = transactionService.transfer(user, request);

        return WebResponse
                .<TransferResponse>builder()
                .message("Successfully transfer")
                .data(transferResponse)
                .build();
    }

    @PostMapping(
            path = "/api/transaction/top-up",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse topUp(User user,
                             @RequestBody TopUpRequest request) {
        transactionService.topUp(user, request);

        return WebResponse
                .<TransferResponse>builder()
                .message("Successfully top up pocket")
                .build();
    }

    @PostMapping(
            path = "/api/transaction/code-pay",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<CreateCodePayResponse> createCodePay(User user,
                                                            @RequestBody CreateCodePayRequest request) {
        String codePay = transactionService.createCodePay(user, request);

        CreateCodePayResponse response = new CreateCodePayResponse();
        response.setCode(codePay);

        return WebResponse
                .<CreateCodePayResponse>builder()
                .message("Successfully create code pay")
                .data(response)
                .build();
    }

    @GetMapping(
            path = "/api/transaction/code-pay/{codePay}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<CodePayResponse> getCodePay(User user,
                                                   @PathVariable("codePay") String codePay) {
        CodePayResponse codePayResponse = transactionService.getCodePay(user, codePay);

        return WebResponse
                .<CodePayResponse>builder()
                .message("Successfully get information code pay")
                .data(codePayResponse)
                .build();
    }

    @PostMapping(
            path = "/api/transaction/code-pay/{codePay}",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<TransferResponse> codePayPayment(User user,
                                                        @PathVariable("codePay") String codePay,
                                                        @RequestBody CodePayRequest request) {

        request.setCode(codePay);
        TransferResponse transferResponse = transactionService.codePayPayment(user, request);

        return WebResponse
                .<TransferResponse>builder()
                .message("Successfully transfer via code pay")
                .data(transferResponse)
                .build();
    }

    @PostMapping(
            path = "/api/transaction/{journal}/reversal",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse reversal(User user,
                                @PathVariable("journal") UUID journal,
                                @RequestBody ReversalRequest request) {

        request.setJournalNumber(journal);
        transactionService.reversal(user, request);

        return WebResponse
                .builder()
                .message("Successfully reversal transaction")
                .build();
    }
}
