package adi_kurniawan.springboot_kash_api.model.transaction;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferRequest {
    @NotNull
    private Integer sourceAccountNumber;

    @NotNull
    private Long amount;

    @NotNull
    private Integer destinationAccountNumber;

    private String description;
}
