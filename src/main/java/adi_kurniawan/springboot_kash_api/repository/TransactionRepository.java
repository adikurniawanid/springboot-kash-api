package adi_kurniawan.springboot_kash_api.repository;

import adi_kurniawan.springboot_kash_api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    //    list
    List<Transaction> findAllBySourceAccountNumberOrDestinationAccountNumber(BigInteger accountNumber, BigInteger sourceAccountNumber);

    Optional<Transaction> findFirstByJournalNumber(UUID journalNumber);
}
