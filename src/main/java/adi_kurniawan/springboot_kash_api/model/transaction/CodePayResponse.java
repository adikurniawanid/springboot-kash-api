package adi_kurniawan.springboot_kash_api.model.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CodePayResponse {
    @NotNull
    private BigInteger destinationAccountNumber;

    @NotNull
    private Long amount;

    private String description;

    @NotNull
    private Date expiredAt;
}
