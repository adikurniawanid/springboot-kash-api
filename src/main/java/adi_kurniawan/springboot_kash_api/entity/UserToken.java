package adi_kurniawan.springboot_kash_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_token", schema = "public")
public class UserToken {
    @Id
    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(name = "verification_token")
    private String verificationToken;

    @Column(name = "verification_token_expired_at")
    private Long verificationTokenExpireedAt;

    @Column(name = "forgot_password_token")
    private String forgotPasswordToken;

    @Column(name = "forgot_password_token_expired_at")
    private Long forgotPasswordTokenExpireedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
