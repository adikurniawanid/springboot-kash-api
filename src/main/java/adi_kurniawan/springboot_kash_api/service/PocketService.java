package adi_kurniawan.springboot_kash_api.service;

import adi_kurniawan.springboot_kash_api.entity.Pocket;
import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.pocket.CreatePocketRequest;
import adi_kurniawan.springboot_kash_api.model.pocket.PocketResponse;
import adi_kurniawan.springboot_kash_api.repository.PocketRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigInteger;
import java.util.List;

@Slf4j
@Service
public class PocketService {

    @Autowired
    private ValidationService validationService;

    @Autowired
    private PocketRepository pocketRepository;

    private PocketResponse mapperResponse(Pocket pocket) {
        return PocketResponse.builder()
                .accountNumber(pocket.getAccountNumber())
                .name(pocket.getName())
                .balance(pocket.getBalance())
                .build();
    }

    @Transactional
    public PocketResponse create(User user, CreatePocketRequest request) {
        validationService.validate(request);

        Pocket pocket = new Pocket();
        pocket.setName(request.getName());
        pocket.setAccountNumber(request.getAccountNumber());
        pocket.setBalance(0L);
        pocket.setUser(user);

        pocketRepository.save(pocket);

        return mapperResponse(pocket);
    }

    @Transactional(readOnly = true)
    public List<PocketResponse> list(User user) {
        List<Pocket> pockets = pocketRepository.findAllByUserId(user.getId());

        return pockets.stream().map(this::mapperResponse).toList();
    }

    @Transactional(readOnly = true)
    public PocketResponse get(User user, BigInteger id) {
        Pocket pocket = pocketRepository.findFirstByUserAndAccountNumber(user, id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Pocket not found")
        );

        return mapperResponse(pocket);
    }

}
