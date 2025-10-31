package com.mgaye.yonei.dto.request;

// ✅ A dedicated request object
public class AttachPaymentMethodRequest {
    private String paymentMethodId;

    // getters and setters
    public String getPaymentMethodId() {
        return paymentMethodId;
    }

    public void setPaymentMethodId(String paymentMethodId) {
        this.paymentMethodId = paymentMethodId;
    }
}
