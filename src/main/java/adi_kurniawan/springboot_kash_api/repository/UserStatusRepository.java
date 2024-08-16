package adi_kurniawan.springboot_kash_api.repository;

import adi_kurniawan.springboot_kash_api.entity.UserStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserStatusRepository extends JpaRepository<UserStatus, String> {
    Optional<UserStatus> findFirstByUserId(Integer id);
}
