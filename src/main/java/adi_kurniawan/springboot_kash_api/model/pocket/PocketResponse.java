package adi_kurniawan.springboot_kash_api.model.pocket;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PocketResponse {
    private BigInteger accountNumber;
    private String name;
    private Long amount;
}
