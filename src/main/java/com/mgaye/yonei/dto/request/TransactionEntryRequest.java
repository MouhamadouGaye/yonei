package com.mgaye.yonei.dto.request;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TransactionEntryRequest {
    private Long userId;
    private Long transferId;
    private BigDecimal amount;
}
