package adi_kurniawan.springboot_kash_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigInteger;
import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "pocket", schema = "public")
public class Pocket {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Integer id;

    @Column(name = "account_number", nullable = false)
    private BigInteger accountNumber;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private Long balance;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "userId", referencedColumnName = "id")
    private User user;
}
