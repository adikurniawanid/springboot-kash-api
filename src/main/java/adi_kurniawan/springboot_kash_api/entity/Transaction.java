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
    private Integer id;

    @Column(name = "source_account_number")
    private BigInteger sourceAccountNumber;

    @Column(name = "destination_account_number")
    private BigInteger destinationAccountNumber;

    private Long amount;

    private String description;

    @Column(name = "journal_number")
    private UUID journalNumber;

    @CreationTimestamp
    private Date timestamp;
}
