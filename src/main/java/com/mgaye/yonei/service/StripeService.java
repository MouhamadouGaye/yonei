package com.mgaye.yonei.service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.mgaye.yonei.dto.stripe.PaymentMethodDTO;
import com.mgaye.yonei.entity.Card;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.CardRepository;
import com.mgaye.yonei.repository.UserRepository;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentIntent;
import com.stripe.model.PaymentMethod;
import com.stripe.model.PaymentMethodCollection;
import com.stripe.model.SetupIntent;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerUpdateParams;
import com.stripe.param.PaymentIntentCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import com.stripe.param.PaymentMethodListParams;
import com.stripe.param.SetupIntentConfirmParams;
import com.stripe.param.SetupIntentCreateParams;

@Service
public class StripeService {

    @Value("${stripe.secret-key}")
    private String stripeSecretKey;

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    public StripeService(CardRepository cardRepository, UserRepository userRepository) {
        this.cardRepository = cardRepository;
        this.userRepository = userRepository;
        Stripe.apiKey = stripeSecretKey;
    }

    /**
     * Create a new Stripe customer
     */
    public String createCustomer(String email, String name) throws StripeException {
        CustomerCreateParams params = CustomerCreateParams.builder()
                .setEmail(email)
                .setName(name)
                .build();

        Customer customer = Customer.create(params);
        return customer.getId();
    }

    /**
     * Create a Setup Intent for adding payment methods
     */
    public String createSetupIntent(String customerId) throws StripeException {
        SetupIntentCreateParams params = SetupIntentCreateParams.builder()
                .setCustomer(customerId)
                .addPaymentMethodType("card")
                .setUsage(SetupIntentCreateParams.Usage.ON_SESSION)
                .build();

        SetupIntent setupIntent = SetupIntent.create(params);
        return setupIntent.getClientSecret();
    }

    /**
     * Confirm and attach a payment method to customer
     */
    public void confirmSetupIntent(String setupIntentId, String paymentMethodId) throws StripeException {
        // Retrieve the setup intent
        SetupIntent setupIntent = SetupIntent.retrieve(setupIntentId);

        // Confirm the setup intent with the payment method
        SetupIntentConfirmParams confirmParams = SetupIntentConfirmParams.builder()
                .setPaymentMethod(paymentMethodId)
                .build();

        setupIntent.confirm(confirmParams);

        // The payment method is now attached to the customer automatically by Stripe
        // when the setup intent is confirmed
    }

    /**
     * Get all payment methods for a customer
     */
    // In StripeService.java - fix the getCustomerPaymentMethods method
    /**
     * Get all payment methods for a customer
     */

    public List<PaymentMethodDTO> getCustomerPaymentMethods(String customerId) throws StripeException {
        PaymentMethodListParams params = PaymentMethodListParams.builder()
                .setCustomer(customerId)
                .setType(PaymentMethodListParams.Type.CARD)
                .build();

        PaymentMethodCollection paymentMethods = PaymentMethod.list(params);

        // Get the user by stripe customer ID to find their default card
        Optional<User> user = userRepository.findByStripeCustomerId(customerId);
        String defaultPaymentMethodId = user
                .flatMap(u -> cardRepository.findByUserIdAndIsDefaultTrue(u.getId()))
                .map(Card::getStripePaymentMethodId)
                .orElse(null);

        return paymentMethods.getData().stream()
                .map(pm -> toPaymentMethodDTO(pm, pm.getId().equals(defaultPaymentMethodId)))
                .collect(Collectors.toList());
    }

    public PaymentMethod getPaymentMethodFromIntent(String paymentIntentId) throws StripeException {
        PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
        String paymentMethodId = paymentIntent.getPaymentMethod();

        if (paymentMethodId != null) {
            return PaymentMethod.retrieve(paymentMethodId);
        }
        return null;
    }

    /**
     * Charge a customer using their saved payment method
     */
    public boolean chargeCustomer(String customerId, String paymentMethodId, String currency, long amountInCents)
            throws StripeException {
        try {
            PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                    .setAmount(amountInCents)
                    .setCurrency(currency.toLowerCase())
                    .setCustomer(customerId)
                    .setPaymentMethod(paymentMethodId)
                    .setConfirm(true)
                    .setOffSession(true)
                    .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
                    .setErrorOnRequiresAction(true)
                    .build();

            PaymentIntent paymentIntent = PaymentIntent.create(params);

            return "succeeded".equals(paymentIntent.getStatus());
        } catch (StripeException e) {
            System.err.println("Stripe charge failed: " + e.getMessage());
            throw e;
        }
    }

    /**
     * Attach a payment method to a customer (alternative method)
     */
    public void attachPaymentMethodToCustomer(String paymentMethodId, String customerId) throws StripeException {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);

        PaymentMethodAttachParams attachParams = PaymentMethodAttachParams.builder()
                .setCustomer(customerId)
                .build();

        paymentMethod.attach(attachParams);
    }

    /**
     * Detach a payment method from customer
     */
    public void detachPaymentMethod(String paymentMethodId) throws StripeException {
        PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
        paymentMethod.detach();
    }

    /**
     * Set default payment method for customer
     */
    public void setDefaultPaymentMethod(String customerId, String paymentMethodId) throws StripeException {
        CustomerUpdateParams params = CustomerUpdateParams.builder()
                .setInvoiceSettings(
                        CustomerUpdateParams.InvoiceSettings.builder()
                                .setDefaultPaymentMethod(paymentMethodId)
                                .build())
                .build();

        Customer customer = Customer.retrieve(customerId);
        customer.update(params);
    }

    /**
     * Convert Stripe PaymentMethod to DTO
     */
    private PaymentMethodDTO toPaymentMethodDTO(PaymentMethod paymentMethod, boolean isDefault) {
        PaymentMethodDTO dto = new PaymentMethodDTO();
        dto.setId(paymentMethod.getId());
        dto.setDefault(isDefault);

        PaymentMethodDTO.CardDetails cardDetails = new PaymentMethodDTO.CardDetails();
        cardDetails.setBrand(paymentMethod.getCard().getBrand());
        cardDetails.setLast4(paymentMethod.getCard().getLast4());
        cardDetails.setExpMonth(paymentMethod.getCard().getExpMonth().intValue());
        cardDetails.setExpYear(paymentMethod.getCard().getExpYear().intValue());

        dto.setCard(cardDetails);
        return dto;
    }

    /**
     * Verify if a payment method belongs to a customer
     */
    public boolean verifyPaymentMethodOwnership(String customerId, String paymentMethodId) throws StripeException {
        try {
            PaymentMethod paymentMethod = PaymentMethod.retrieve(paymentMethodId);
            return customerId.equals(paymentMethod.getCustomer());
        } catch (StripeException e) {
            return false;
        }
    }

    /**
     * Create a Payment Intent for one-time payments
     */
    public String createPaymentIntent(long amount, String currency, String customerId, String paymentMethodId)
            throws StripeException {
        PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
                .setAmount(amount)
                .setCurrency(currency.toLowerCase())
                .setCustomer(customerId)
                .setPaymentMethod(paymentMethodId)
                .setConfirm(true)
                .setConfirmationMethod(PaymentIntentCreateParams.ConfirmationMethod.AUTOMATIC)
                .setReturnUrl("your-app://stripe-redirect")
                .build();

        PaymentIntent paymentIntent = PaymentIntent.create(params);
        return paymentIntent.getClientSecret();
    }
}