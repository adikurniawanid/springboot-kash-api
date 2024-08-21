package adi_kurniawan.springboot_kash_api.model.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodePayRequest {
    @NotNull
    private String code;

    @NotNull
    private BigInteger accountNumber;

    @NotNull
    private String pin;
    
    private String description;
}
