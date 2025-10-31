package com.mgaye.yonei.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentMethodDTO {
    private String id;
    private CardDetails card;
    private boolean isDefault;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CardDetails {
        private String brand;
        private String last4;
        private int expMonth;
        private int expYear;
    }
}