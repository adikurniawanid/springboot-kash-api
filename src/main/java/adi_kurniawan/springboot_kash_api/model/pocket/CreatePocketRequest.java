package adi_kurniawan.springboot_kash_api.model.pocket;

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
public class CreatePocketRequest {
    @NotNull
    @Size(min = 1, max = 100)
    private String name;
}
