package com.mgaye.yonei.dto.request;

import lombok.Data;
import java.math.BigDecimal;

// @Data
// public class TransferRequestDto {
//     private Long fromUserId;
//     private Long toUserId;
//     private Long beneficiaryId; // optionnel
//     private BigDecimal amount;
// }

@Data
public class TransferRequestDto {
    // For internal transfers: recipient must be an existing user in the system
    private Long toUserId;

    // For external transfers: recipient is one of the sender's saved beneficiaries
    private Long beneficiaryId;

    // Transfer amount (must be > 0, validated in controller)
    private BigDecimal amount;

    // If true → transfer is funded via card (skip balance check on sender account)
    // If false → transfer is funded from sender's internal balance
    private boolean fromCard = false;
}
