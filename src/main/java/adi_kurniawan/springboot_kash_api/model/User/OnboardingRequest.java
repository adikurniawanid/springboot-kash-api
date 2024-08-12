package adi_kurniawan.springboot_kash_api.model.User;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OnboardingRequest {
    private String name;
    private String phone;
    private String avatarUrl;
    private UUID publicId;
}
