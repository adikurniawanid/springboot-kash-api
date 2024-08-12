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

public class UserResponse {
    private UUID publicId;
    private String username;
    private String email;
    private String name;
    private String phone;
    private String avatarUrl;
}
