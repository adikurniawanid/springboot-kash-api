package adi_kurniawan.springboot_kash_api.repository;

import adi_kurniawan.springboot_kash_api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
    List<Transaction> findAllBySourceAccountNumber(BigInteger accountNumber);

    List<Transaction> findAllBySourceAccountNumberOrDestinationAccountNumber(BigInteger accountNumber, BigInteger sourceAccountNumber);

    List<Transaction> findAllByDestinationAccountNumber(BigInteger accountNumber);
}
