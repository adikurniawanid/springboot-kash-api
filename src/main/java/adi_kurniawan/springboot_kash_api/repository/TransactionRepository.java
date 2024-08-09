package adi_kurniawan.springboot_kash_api.repository;

import adi_kurniawan.springboot_kash_api.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Integer> {
}
