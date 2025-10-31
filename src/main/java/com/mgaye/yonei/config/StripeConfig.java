package com.mgaye.yonei.config;

import com.stripe.Stripe;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

import jakarta.annotation.PostConstruct;

@Configuration
public class StripeConfig {

    @Value("${stripe.secret-key}")
    private String secretKey;

    @PostConstruct
    public void init() {
        // ✅ Initialize Stripe with your key
        Stripe.apiKey = secretKey;
    }
}

// Visa: 4242 4242 4242 4242

// Mastercard: 5555 5555 5555 4444

// Use any future expiry date (e.g. 12/34) and any 3-digit CVC.