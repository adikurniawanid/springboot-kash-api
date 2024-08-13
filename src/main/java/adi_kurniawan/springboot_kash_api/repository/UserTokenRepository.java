package adi_kurniawan.springboot_kash_api.repository;

import adi_kurniawan.springboot_kash_api.entity.UserToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserTokenRepository extends JpaRepository<UserToken, String> {
    Optional<UserToken> findFirstByVerificationToken(String token);

}
