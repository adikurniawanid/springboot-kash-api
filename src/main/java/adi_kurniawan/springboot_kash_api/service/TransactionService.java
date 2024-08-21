package adi_kurniawan.springboot_kash_api.service;


import adi_kurniawan.springboot_kash_api.entity.Pocket;
import adi_kurniawan.springboot_kash_api.entity.Transaction;
import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.transaction.*;
import adi_kurniawan.springboot_kash_api.repository.PocketRepository;
import adi_kurniawan.springboot_kash_api.repository.TransactionRepository;
import adi_kurniawan.springboot_kash_api.security.BCrypt;
import com.auth0.jwt.interfaces.Claim;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.*;

@Service
public class TransactionService {
    private static final Logger log = LoggerFactory.getLogger(TransactionService.class);
    @Autowired
    private ValidationService validationService;

    @Autowired
    private PocketRepository pocketRepository;

    @Autowired
    private TransactionRepository transactionRepository;
    @Autowired
    private TokenService tokenService;

    private boolean isSufficientBalance(Long sourceAmount, Long transactionAmount) {
        return sourceAmount >= transactionAmount;
    }

    @Transactional(readOnly = true)
    public InquiryResponse inquiry(User user, BigInteger accountNumber) {
        if (accountNumber.equals(BigInteger.ZERO)) {
            return InquiryResponse.builder()
                    .name("TOP UP SERVICE")
                    .build();
        }
        Pocket pocket = pocketRepository.findFirstByAccountNumber(accountNumber).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Account not found")
        );

        return InquiryResponse.builder()
                .name(pocket.getUser().getUserDetail().getName())
                .build();
    }

    @Transactional
    public TransferResponse transfer(User user, TransferRequest request) {
        validationService.validate(request);

        Pocket sourcePocket = pocketRepository.findFirstByUserAndAccountNumber(user, request.getSourceAccountNumber()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Source account not found")
        );

        Pocket destinationPocket = pocketRepository.findFirstByAccountNumber(request.getDestinationAccountNumber()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Destination account not found")
        );


        if (!(isSufficientBalance(sourcePocket.getBalance(), request.getAmount()))) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Insufficient balance");
        }

        if (BCrypt.checkpw(request.getPin(), user.getPin())) {

            sourcePocket.setBalance(sourcePocket.getBalance() - request.getAmount());
            destinationPocket.setBalance(destinationPocket.getBalance() + request.getAmount());
            pocketRepository.save(sourcePocket);
            pocketRepository.save(destinationPocket);

            Transaction transactionOutgoing = new Transaction();

            transactionOutgoing.setSourceAccountNumber(sourcePocket.getAccountNumber());
            transactionOutgoing.setDestinationAccountNumber(destinationPocket.getAccountNumber());
            transactionOutgoing.setAmount(request.getAmount());
            transactionOutgoing.setDescription(request.getDescription());
            transactionOutgoing.setJournalNumber(UUID.randomUUID());
            transactionRepository.save(transactionOutgoing);


            return TransferResponse.builder()
                    .journalNumber(transactionOutgoing.getJournalNumber())
                    .senderName(sourcePocket.getUser().getUserDetail().getName())
                    .sourceAccountNumber(transactionOutgoing.getSourceAccountNumber())
                    .receiverName(destinationPocket.getUser().getUserDetail().getName())
                    .destinationAccountNumber(transactionOutgoing.getDestinationAccountNumber())
                    .amount(transactionOutgoing.getAmount())
                    .description(transactionOutgoing.getDescription())
                    .timestamp(transactionOutgoing.getTimestamp())
                    .build();
        } else {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Wrong PIN, please try again");
        }
    }

    @Transactional(readOnly = true)
    public List<HistoryResponse> history(User user, BigInteger accountNumber) {
        Pocket pocket = pocketRepository.findFirstByUserAndAccountNumber(user, accountNumber).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pocket not found")
        );


        List<Transaction> history = transactionRepository.findAllBySourceAccountNumberOrDestinationAccountNumber(pocket.getAccountNumber(), pocket.getAccountNumber());

        return history.stream().map(transaction -> HistoryResponse.builder()
                        .journalNumber(transaction.getJournalNumber())
                        .senderName(inquiry(user, transaction.getSourceAccountNumber()).getName())
                        .sourceAccountNumber(transaction.getSourceAccountNumber())
                        .receiverName(inquiry(user, transaction.getDestinationAccountNumber()).getName())
                        .destinationAccountNumber(transaction.getDestinationAccountNumber())
                        .amount(transaction.getAmount())
                        .description(transaction.getDescription())
                        .timestamp(transaction.getTimestamp())
                        .type(Objects.equals(transaction.getSourceAccountNumber(), accountNumber) ? "(-)KREDIT" : "(+)DEBET")
                        .build()
                )
                .toList();
    }

    @Transactional
    public void topUp(User user, TopUpRequest request) {
        Pocket pocket = pocketRepository.findFirstByUserAndAccountNumber(user, request.getAccountNumber()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pocket not found")
        );

        pocket.setBalance(pocket.getBalance() + request.getAmount());
        pocketRepository.save(pocket);

        Transaction topUpTransaction = new Transaction();

        topUpTransaction.setSourceAccountNumber(request.getAccountNumber());
        topUpTransaction.setDestinationAccountNumber(request.getAccountNumber());
        topUpTransaction.setAmount(request.getAmount());
        topUpTransaction.setDescription("TOP UP POCKET TRANSACTION");
        topUpTransaction.setJournalNumber(UUID.randomUUID());
        transactionRepository.save(topUpTransaction);
    }

    public String createCodePay(User user, CreateCodePayRequest request) {
        validationService.validate(request);

        pocketRepository.findFirstByUserAndAccountNumber(user, request.getDestinationAccountNumber()).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pocket not found")
        );

        return tokenService.generateCodePay(request);
    }

    public CodePayResponse getCodePay(User user, String codePay) {

        Map<String, Claim> codePayPayload = tokenService.codePayValidate(codePay);

        CodePayResponse codePayResponse = new CodePayResponse();
        codePayResponse.setAmount(codePayPayload.get("amount").asLong());
        codePayResponse.setDestinationAccountNumber(new BigInteger(codePayPayload.get("destinationAccountNumber").toString().replaceAll("\"", "")));
        codePayResponse.setDescription(codePayPayload.get("description").toString().replaceAll("\"", ""));
        codePayResponse.setExpiredAt(new Date((codePayPayload.get("expiredAt").asLong())));

        return codePayResponse;
    }

    public TransferResponse codePayPayment(User user, CodePayRequest request) {
        validationService.validate(request);
        Map<String, Claim> codePayPayload = tokenService.codePayValidate(request.getCode());

        TransferRequest transferRequest = new TransferRequest();
        transferRequest.setSourceAccountNumber(request.getAccountNumber());
        transferRequest.setDestinationAccountNumber(new BigInteger(codePayPayload.get("destinationAccountNumber").toString().replaceAll("\"", "")));
        transferRequest.setAmount(codePayPayload.get("amount").asLong());
        transferRequest.setDescription(Objects.nonNull(request.getDescription()) ? "CODEPAY " + request.getDescription() : "CODEPAY " + codePayPayload.get("description").toString().replaceAll("\"", ""));
        transferRequest.setPin(request.getPin());

        return transfer(user, transferRequest);
    }
}
