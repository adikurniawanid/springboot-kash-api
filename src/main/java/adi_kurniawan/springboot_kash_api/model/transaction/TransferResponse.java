package adi_kurniawan.springboot_kash_api.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse {
    private BigInteger sourceAccountNumber;
    private Long amount;
    private BigInteger destinationAccountNumber;
    private String description;
}
