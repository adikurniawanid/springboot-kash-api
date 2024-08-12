package adi_kurniawan.springboot_kash_api.repository;

import adi_kurniawan.springboot_kash_api.entity.Pocket;
import adi_kurniawan.springboot_kash_api.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.math.BigInteger;
import java.util.List;
import java.util.Optional;


@Repository
public interface PocketRepository extends JpaRepository<Pocket, Integer> {
    Optional<Pocket> findFirstByUserAndAccountNumber(User user, Integer id);

    Optional<Pocket> findFirstByAccountNumber(BigInteger id);

    List<Pocket> findAllByUserId(Integer userId);
}
