package adi_kurniawan.springboot_kash_api.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.util.Date;

@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "user_detail", schema = "public")
public class UserDetail {
    @Id
    @Column(name = "user_id", nullable = false)
    private int userId;

    @Column(nullable = false)
    private String name;

    @Column(unique = true)
    private String phone;

    @Column(name = "avatar_url")
    private String avatarUrl;

    @CreationTimestamp
    @Column(updatable = false, nullable = false)
    private Date createdAt;

    @UpdateTimestamp
    private Date updatedAt;

    @OneToOne(cascade = CascadeType.ALL)
    @MapsId
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    private User user;
}
