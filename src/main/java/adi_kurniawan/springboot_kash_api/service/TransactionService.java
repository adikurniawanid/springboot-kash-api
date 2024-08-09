package adi_kurniawan.springboot_kash_api.service;


import adi_kurniawan.springboot_kash_api.entity.Pocket;
import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.transaction.InquiryRequest;
import adi_kurniawan.springboot_kash_api.model.transaction.InquiryResponse;
import adi_kurniawan.springboot_kash_api.model.transaction.TransferRequest;
import adi_kurniawan.springboot_kash_api.model.transaction.TransferResponse;
import adi_kurniawan.springboot_kash_api.repository.PocketRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class TransactionService {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private PocketRepository pocketRepository;

    private boolean isSufficientBalance(Long sourceAmount, Long transactionAmount) {
        return sourceAmount >= transactionAmount;
    }


    @Transactional(readOnly = true)
    public InquiryResponse inquiry(User user, InquiryRequest request) {
        validationService.validate(request);

        Pocket pocket = pocketRepository.findFirstByAccountNumber(request.getAccountNumber()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")
        );

        return InquiryResponse.builder()
                .name(pocket.getUser().getUsername())
                .balance(pocket.getBalance())
                .build();
    }

    @Transactional
    public TransferResponse transfer(User user, TransferRequest request) {
        validationService.validate(request);

        Pocket sourcePocket = pocketRepository.findFirstByAccountNumber(request.getSourceAccountNumber()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found")
        );

        Pocket destinationPocket = pocketRepository.findFirstByAccountNumber(request.getDestinationAccountNumber()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found")
        );


        if (!(isSufficientBalance(sourcePocket.getBalance(), request.getAmount()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient balance");
        }

        sourcePocket.setBalance(sourcePocket.getBalance() - request.getAmount());
        destinationPocket.setBalance(destinationPocket.getBalance() + request.getAmount());
        pocketRepository.save(sourcePocket);
        pocketRepository.save(destinationPocket);

        return TransferResponse.builder()
                .sourceAccountNumber(sourcePocket.getAccountNumber())
                .destinationAccountNumber(destinationPocket.getAccountNumber())
                .amount(request.getAmount())
                .description(request.getDescription())
                .build();
    }
}
