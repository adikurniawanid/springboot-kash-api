package adi_kurniawan.springboot_kash_api.controller;

import adi_kurniawan.springboot_kash_api.entity.User;
import adi_kurniawan.springboot_kash_api.model.WebResponse;
import adi_kurniawan.springboot_kash_api.model.pocket.CreatePocketRequest;
import adi_kurniawan.springboot_kash_api.model.pocket.PocketResponse;
import adi_kurniawan.springboot_kash_api.service.PocketService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
public class PocketController {

    @Autowired
    private PocketService pocketService;

    @PostMapping(
            path = "/api/pocket",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<PocketResponse> create(User user,
                                              @RequestBody CreatePocketRequest request) {
        PocketResponse createdPocket = pocketService.create(user, request);

        return WebResponse
                .<PocketResponse>builder()
                .message("Pocket created successfully")
                .data(createdPocket)
                .build();
    }

    @GetMapping(
            path = "api/pocket",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<List<PocketResponse>> list(User user) {
        List<PocketResponse> pocketListResponse = pocketService.list(user);

        return WebResponse
                .<List<PocketResponse>>builder()
                .message("Successfully retrieved list of pockets")
                .data(pocketListResponse)
                .build();
    }


    @GetMapping(
            path = "api/pocket/{pocketId}",
            produces = MediaType.APPLICATION_JSON_VALUE)
    public WebResponse<PocketResponse> get(User user,
                                           @PathVariable("pocketId") Integer id) {

        PocketResponse pocketResponse = pocketService.get(user, id);
        return WebResponse
                .<PocketResponse>builder()
                .message("Get pocket detail successfully")
                .data(pocketResponse)
                .build();
    }
}
