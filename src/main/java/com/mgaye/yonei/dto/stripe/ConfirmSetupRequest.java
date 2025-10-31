package com.mgaye.yonei.dto.stripe;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConfirmSetupRequest {
    private String setupIntentId;
    private String paymentMethodId;
}