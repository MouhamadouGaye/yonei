package com.mgaye.yonei.dto;

import com.mgaye.yonei.entity.TransactionEntry;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

// @Data
// public class TransactionEntryDto {
//     private Long id;
//     private Long userId;
//     private Long transferId;
//     private Long prevEntryId;
//     private Long nextEntryId;
//     private Instant createdAt;

//     public static TransactionEntryDto fromEntity(TransactionEntry e) {
//         TransactionEntryDto d = new TransactionEntryDto();
//         d.setId(e.getId());
//         d.setUserId(e.getUser().getId());
//         d.setTransferId(e.getTransfer().getId());
//         d.setPrevEntryId(e.getPrevEntryId());
//         d.setNextEntryId(e.getNextEntryId());
//         d.setCreatedAt(e.getCreatedAt());
//         return d;
//     }
// }
@Data
public class TransactionEntryDto {
    private Long id;
    private Long userId;
    private Long transferId;
    private Long prevEntryId;
    private Long nextEntryId;
    private Instant createdAt;

    private BigDecimal amount;
    private BigDecimal balanceAfter;

    // Transfer info
    private String status;
    private Long fromUserId;
    private Long toUserId;

    public static TransactionEntryDto fromEntity(TransactionEntry e) {
        TransactionEntryDto d = new TransactionEntryDto();
        d.setId(e.getId());
        d.setUserId(e.getUser().getId());
        d.setTransferId(e.getTransfer().getId());
        d.setPrevEntryId(e.getPrevEntryId());
        d.setNextEntryId(e.getNextEntryId());
        d.setCreatedAt(e.getCreatedAt());

        d.setAmount(e.getAmount());
        d.setBalanceAfter(e.getBalanceAfter());

        // transfer details
        if (e.getTransfer() != null) {
            d.setStatus(e.getTransfer().getStatus().name());
            d.setFromUserId(e.getTransfer().getFromUser().getId());
            d.setToUserId(
                    e.getTransfer().getToUser() != null ? e.getTransfer().getToUser().getId() : null);
        }

        return d;
    }
}
