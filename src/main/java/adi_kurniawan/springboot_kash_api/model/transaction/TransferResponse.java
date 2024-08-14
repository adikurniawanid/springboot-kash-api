package adi_kurniawan.springboot_kash_api.model.transaction;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigInteger;
import java.util.Date;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TransferResponse {
    private Date timestamp;
    private UUID journalNumber;
    private String senderName;
    private BigInteger sourceAccountNumber;
    private Long amount;
    private String receiverName;
    private BigInteger destinationAccountNumber;
    private String description;

}
