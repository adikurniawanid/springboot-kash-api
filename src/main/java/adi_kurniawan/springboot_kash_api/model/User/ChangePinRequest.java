package adi_kurniawan.springboot_kash_api.model.User;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangePinRequest {
    @NotNull
    private String oldPin;
    @NotNull
    private String newPin;
}
