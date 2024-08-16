package adi_kurniawan.springboot_kash_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_status", schema = "public")
public class UserStatus {
    @Id
    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "email_verified_at")
    private Date emailVerifiedAt;

    @Column(name = "onboarded_at")
    private Date onboardedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
