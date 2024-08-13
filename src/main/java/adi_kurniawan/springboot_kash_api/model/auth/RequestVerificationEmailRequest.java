package adi_kurniawan.springboot_kash_api.model.auth;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RequestVerificationEmailRequest {
    @NotNull
    private String username;
}
