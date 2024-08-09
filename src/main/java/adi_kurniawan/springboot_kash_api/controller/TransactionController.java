package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.transaction.InquiryRequest;
import adi_kurniawan.springboot_kash_api.model.transaction.InquiryResponse;
import adi_kurniawan.springboot_kash_api.model.transaction.TransferRequest;
import adi_kurniawan.springboot_kash_api.model.transaction.TransferResponse;
import adi_kurniawan.springboot_kash_api.service.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class TransactionController {

    @Autowired
    private TransactionService transactionService;

    @GetMapping(
            path = "api/inquiry",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<InquiryResponse> list(User user,
                                             @RequestBody InquiryRequest request) {
        InquiryResponse inquiredUser = transactionService.inquiry(user, request);

        return WebResponse
                .<InquiryResponse>builder()
                .message("Successfully inquiry account")
                .data(inquiredUser)
                .build();
    }

    @PostMapping(
            path = "/api/transfer",
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
}
