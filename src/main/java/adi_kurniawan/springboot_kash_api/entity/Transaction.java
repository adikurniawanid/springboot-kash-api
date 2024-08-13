package adi_kurniawan.springboot_kash_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "transaction", schema = "public")
public class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "source_account_number", nullable = false)
    private BigInteger sourceAccountNumber;

    @Column(name = "destination_account_number", nullable = false)
    private BigInteger destinationAccountNumber;

    @Column(nullable = false)
    private Long amount;

    private String description;

    @Column(unique = true, updatable = false, name = "journal_number", nullable = false)
    private UUID journalNumber;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Date timestamp;
}
