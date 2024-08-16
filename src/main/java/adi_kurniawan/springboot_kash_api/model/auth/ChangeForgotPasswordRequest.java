package adi_kurniawan.springboot_kash_api.model.auth;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ChangeForgotPasswordRequest {
    @NotNull
    @Size(min = 8, max = 100)
    @Email
    private String email;

    @NotNull
    @Size(min = 6, max = 6)
    private String otp;

    @NotNull
    @Size(min = 12, max = 100)
    private String newPassword;
}
