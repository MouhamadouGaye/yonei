package com.mgaye.yonei.service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentConfirmParams;
import com.stripe.param.PaymentIntentCreateParams;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Service
public class PaymentService {

    // public boolean chargeCard(String paymentMethodId, String currency, long
    // amountInCents) {
    // try {
    // Map<String, Object> params = new HashMap<>();
    // params.put("amount", amountInCents); // Stripe requires cents
    // params.put("currency", currency); // e.g., "usd", "eur"
    // params.put("payment_method", paymentMethodId);
    // params.put("confirm", true);

    // PaymentIntent intent = PaymentIntent.create(params);

    // return "succeeded".equals(intent.getStatus());

    // } catch (StripeException e) {
    // e.printStackTrace();
    // return false;
    // }
    // } // Map pattern that does the same thing this builder pattern does below

    // public boolean chargeCard(String paymentMethodId, String currency, long
    // amountInCents) throws StripeException {

    // try {
    // PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
    // .setAmount(amountInCents)
    // .setCurrency(currency)
    // .setPaymentMethod(paymentMethodId)
    // .setConfirm(true)
    // .build();

    // PaymentIntent intent = PaymentIntent.create(params);
    // return "succeeded".equals(intent.getStatus());
    // } catch (StripeException e) {
    // // Log the exception or handle it as needed
    // e.printStackTrace();
    // throw e; // Rethrow the exception to be handled by the caller
    // }

    // }

    // public boolean chargeCard(String paymentMethodId, String currency, long
    // amountInCents) throws StripeException {
    // try {
    // // 1️⃣ Create a PaymentIntent with the card details
    // PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
    // .setAmount(amountInCents)
    // .setCurrency(currency)
    // .setPaymentMethod(paymentMethodId)
    // .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
    // // more explicit
    // .setConfirm(true) // try to confirm immediately
    // .setReturnUrl("http://localhost:4200/payment-result") // ⬅️ required here
    // .build();

    // PaymentIntent intent = PaymentIntent.create(createParams);

    // // 2️⃣ Check PaymentIntent status
    // switch (intent.getStatus()) {
    // case "succeeded":
    // // ✅ Payment completed instantly
    // return true;

    // case "requires_action":
    // // ⚠️ Payment requires additional action (3D Secure, SCA)
    // PaymentIntentConfirmParams confirmParams =
    // PaymentIntentConfirmParams.builder()
    // .setReturnUrl("http://localhost:4200/payment-result")
    // // ⬆️ This is where Stripe will redirect the user after 3DS
    // .build();

    // intent = intent.confirm(confirmParams);

    // // After redirect + authentication, Stripe will finalize the payment
    // return "succeeded".equals(intent.getStatus());

    // default:
    // // ❌ All other states (requires_payment_method, canceled, etc.)
    // return false;
    // }

    // } catch (StripeException e) {
    // // Log & rethrow
    // e.printStackTrace();
    // throw e;
    // }
    // }

    public boolean chargeCard(String customerId, String paymentMethodId, String currency, long amountInCents)
            throws StripeException {
        try {
            // 1️⃣ Create a PaymentIntent for the Customer
            PaymentIntentCreateParams createParams = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency)
                    .setCustomer(customerId) // ✅ Always include the Customer
                    .setPaymentMethod(paymentMethodId) // ✅ Use the customer's attached card
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.MANUAL)
                    .setConfirm(true) // try to confirm immediately
                    .setReturnUrl("http://localhost:4200/payment-result")
                    .build();

            PaymentIntent intent = PaymentIntent.create(createParams);

            // 2️⃣ Handle PaymentIntent status
            switch (intent.getStatus()) {
                case "succeeded":
                    return true; // ✅ payment done

                case "requires_action":
                    PaymentIntentConfirmParams confirmParams = PaymentIntentConfirmParams.builder()
                            .setReturnUrl("http://localhost:4200/payment-result")
                            .build();

                    intent = intent.confirm(confirmParams);
                    return "succeeded".equals(intent.getStatus());

                default:
                    return false;
            }

        } catch (StripeException e) {
            e.printStackTrace();
            throw e;
        }
    }

}
