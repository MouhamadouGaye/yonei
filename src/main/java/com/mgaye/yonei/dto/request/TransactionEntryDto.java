package com.mgaye.yonei.dto.request;

import java.math.BigDecimal;
import java.time.Instant;

import com.mgaye.yonei.entity.TransactionEntry;
import com.mgaye.yonei.entity.Transfer;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TransactionEntryDto {
    private Long id;
    private Instant createdAt;
    private Long prevEntryId;
    private Long nextEntryId;
    private BigDecimal amount;
    private BigDecimal balanceAfter;
    private Long fromUserId;
    private Long toUserId;
    private String status;

    public static TransactionEntryDto fromEntity(TransactionEntry entry) {
        Transfer transfer = entry.getTransfer();

        return TransactionEntryDto.builder()
                .id(entry.getId())
                .createdAt(entry.getCreatedAt())
                .prevEntryId(entry.getPrevEntryId())
                .nextEntryId(entry.getNextEntryId())
                .amount(entry.getAmount())
                .balanceAfter(entry.getBalanceAfter())
                .fromUserId(transfer.getFromUser() != null ? transfer.getFromUser().getId() : null)
                .toUserId(transfer.getToUser() != null ? transfer.getToUser().getId() : null)
                .status(transfer.getStatus().name())
                .build();
    }
}
