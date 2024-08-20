package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.transaction.InquiryResponse;
import adi_kurniawan.springboot_kash_api.model.transaction.TopUpRequest;
import adi_kurniawan.springboot_kash_api.model.transaction.TransferRequest;
import adi_kurniawan.springboot_kash_api.model.transaction.TransferResponse;
import adi_kurniawan.springboot_kash_api.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.math.BigInteger;

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
}
