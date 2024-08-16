package adi_kurniawan.springboot_kash_api.model.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthResponse {
    private UUID publicId;
    private String name;
    private String username;
    private String email;
    private String accessToken;
}
