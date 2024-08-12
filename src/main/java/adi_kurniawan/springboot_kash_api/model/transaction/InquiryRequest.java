package adi_kurniawan.springboot_kash_api.model.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class InquiryRequest {
    @NotNull
    private BigInteger accountNumber;
}
