package com.mgaye.yonei.dto.response;

import com.mgaye.yonei.entity.Transfer;
import lombok.Data;

import java.math.BigDecimal;
import java.time.Instant;

@Data
public class TransferResponseDto {
    private Long id;
    private Long fromUserId;
    private Long toUserId;
    private BigDecimal amount;
    private Instant createdAt;
    private String status;

    public static TransferResponseDto fromEntity(Transfer t) {
        TransferResponseDto d = new TransferResponseDto();
        d.setId(t.getId());
        d.setFromUserId(t.getFromUser().getId());
        d.setToUserId(t.getToUser().getId());
        d.setAmount(t.getAmount());
        d.setCreatedAt(t.getCreatedAt());
        d.setStatus(t.getStatus().name());
        return d;
    }
}
