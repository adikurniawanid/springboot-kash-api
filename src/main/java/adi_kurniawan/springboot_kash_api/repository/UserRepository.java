package adi_kurniawan.springboot_kash_api.repository;

import adi_kurniawan.springboot_kash_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    Optional<User> findFirstByUsername(String username);

    Optional<User> findFirstByEmail(String email);

    Optional<User> findFirstByPublicId(UUID publicId);

    Optional<User> findFirstById(Integer id);
}
