package adi_kurniawan.springboot_kash_api.service;


import adi_kurniawan.springboot_kash_api.entity.Pocket;
import adi_kurniawan.springboot_kash_api.entity.Transaction;
import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.transaction.*;
import adi_kurniawan.springboot_kash_api.repository.PocketRepository;
import adi_kurniawan.springboot_kash_api.repository.TransactionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Service
public class TransactionService {

    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    @Autowired
    private ValidationService validationService;

    @Autowired
    private PocketRepository pocketRepository;

    @Autowired
    private TransactionRepository transactionRepository;

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

        Transaction transactionOutgoing = new Transaction();

        transactionOutgoing.setSourceAccountNumber(sourcePocket.getAccountNumber());
        transactionOutgoing.setDestinationAccountNumber(destinationPocket.getAccountNumber());
        transactionOutgoing.setAmount(request.getAmount());
        transactionOutgoing.setDescription(request.getDescription());
        transactionOutgoing.setTimestamp(new Date());
        transactionOutgoing.setJournalNumber(UUID.randomUUID());
        transactionRepository.save(transactionOutgoing);


        return TransferResponse.builder()
                .journalNumber(transactionOutgoing.getJournalNumber())
                .sourceAccountNumber(transactionOutgoing.getSourceAccountNumber())
                .destinationAccountNumber(transactionOutgoing.getDestinationAccountNumber())
                .amount(transactionOutgoing.getAmount())
                .description(transactionOutgoing.getDescription())
                .timestamp(transactionOutgoing.getTimestamp())
                .build();
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> history(User user, BigInteger accountNumber) {
        Pocket pocket = pocketRepository.findFirstByAccountNumber(accountNumber).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pocket not found")
        );

        List<Transaction> history = transactionRepository.findAllBySourceAccountNumberOrDestinationAccountNumber(pocket.getAccountNumber(), pocket.getAccountNumber());


        return history.stream().map(transaction -> {
            return HistoryResponse.builder()
                    .journalNumber(transaction.getJournalNumber())
                    .sourceAccountNumber(transaction.getSourceAccountNumber())
                    .destinationAccountNumber(transaction.getDestinationAccountNumber())
                    .amount(transaction.getAmount())
                    .description(transaction.getDescription())
                    .timestamp(transaction.getTimestamp())
                    .type(Objects.equals(transaction.getSourceAccountNumber(), accountNumber) ? "KREDIT" : "DEBET")
                    .build();
        }).toList();


    }
}
