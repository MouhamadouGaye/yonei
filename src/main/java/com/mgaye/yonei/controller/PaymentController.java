package com.mgaye.yonei.controller;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgaye.yonei.dto.stripe.ConfirmSetupRequest;
import com.mgaye.yonei.dto.stripe.PaymentMethodDTO;
import com.mgaye.yonei.entity.Card;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.CardRepository;
import com.mgaye.yonei.repository.UserRepository;
import com.mgaye.yonei.service.StripeService;
import com.stripe.model.PaymentMethod;

// Enhanced PaymentController with more endpoints
@RestController
@RequestMapping("/api/payments")
@PreAuthorize("isAuthenticated()")
public class PaymentController {

    @Autowired
    private StripeService stripeService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CardRepository cardRepository;

    @PostMapping("/setup-intent")
    public ResponseEntity<?> createSetupIntent(Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create or get Stripe customer
            String customerId = user.getStripeCustomerId();
            if (customerId == null) {
                customerId = stripeService.createCustomer(user.getEmail(), user.getUsername());
                user.setStripeCustomerId(customerId);
                userRepository.save(user);
            }

            // Create setup intent
            String clientSecret = stripeService.createSetupIntent(customerId);

            Map<String, String> response = new HashMap<>();
            response.put("clientSecret", clientSecret);
            response.put("customerId", customerId);

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/confirm-setup")
    public ResponseEntity<?> confirmSetupIntent(@RequestBody ConfirmSetupRequest request) {
        try {
            stripeService.confirmSetupIntent(request.getSetupIntentId(), request.getPaymentMethodId());
            return ResponseEntity.ok().body(Map.of("message", "Payment method added successfully"));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/methods")
    public ResponseEntity<?> getPaymentMethods(Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getStripeCustomerId() == null) {
                return ResponseEntity.ok(List.of());
            }

            List<PaymentMethodDTO> methods = stripeService.getCustomerPaymentMethods(user.getStripeCustomerId());
            return ResponseEntity.ok(methods);

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // In PaymentController.java - fix the setDefaultPaymentMethod method
    @PostMapping("/default-method")
    public ResponseEntity<?> setDefaultPaymentMethod(@RequestBody Map<String, String> request, Principal principal) {
        try {
            String paymentMethodId = request.get("paymentMethodId");
            if (paymentMethodId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment method ID is required"));
            }

            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getStripeCustomerId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User has no Stripe customer"));
            }

            // Verify the payment method belongs to the user
            if (!stripeService.verifyPaymentMethodOwnership(user.getStripeCustomerId(), paymentMethodId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment method does not belong to user"));
            }

            // Set as default in Stripe
            stripeService.setDefaultPaymentMethod(user.getStripeCustomerId(), paymentMethodId);

            // Update local database
            // First, unset all other default cards for this user
            List<Card> userCards = cardRepository.findByUserId(user.getId());
            userCards.forEach(card -> {
                boolean isNowDefault = card.getStripePaymentMethodId().equals(paymentMethodId);
                if (card.getIsDefault() != isNowDefault) {
                    card.setIsDefault(isNowDefault);
                    cardRepository.save(card);
                }
            });

            // Update user's default payment method
            user.setStripePaymentMethodId(paymentMethodId);
            userRepository.save(user);

            return ResponseEntity.ok().body(Map.of("message", "Default payment method updated successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    // Fix the detachPaymentMethod method
    @DeleteMapping("/methods/{paymentMethodId}")
    public ResponseEntity<?> detachPaymentMethod(@PathVariable String paymentMethodId, Principal principal) {
        try {
            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getStripeCustomerId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User has no Stripe customer"));
            }

            // Verify the payment method belongs to the user
            if (!stripeService.verifyPaymentMethodOwnership(user.getStripeCustomerId(), paymentMethodId)) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment method does not belong to user"));
            }

            // Check if this is the user's default payment method
            Optional<Card> currentCard = cardRepository.findByStripePaymentMethodId(paymentMethodId);
            if (currentCard.isPresent() && currentCard.get().getIsDefault()) {
                // Find another payment method to set as default
                Optional<Card> otherCard = cardRepository.findByUserId(user.getId())
                        .stream()
                        .filter(card -> !card.getStripePaymentMethodId().equals(paymentMethodId))
                        .findFirst();

                if (otherCard.isPresent()) {
                    // Update the other card as default
                    otherCard.get().setIsDefault(true);
                    cardRepository.save(otherCard.get());
                    user.setStripePaymentMethodId(otherCard.get().getStripePaymentMethodId());
                } else {
                    user.setStripePaymentMethodId(null);
                }
                userRepository.save(user);
            }

            // Delete from local database
            currentCard.ifPresent(cardRepository::delete);

            // Detach from Stripe
            stripeService.detachPaymentMethod(paymentMethodId);

            return ResponseEntity.ok().body(Map.of("message", "Payment method removed successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/attach-method")
    public ResponseEntity<?> attachPaymentMethod(@RequestBody Map<String, String> request, Principal principal) {
        try {
            String paymentMethodId = request.get("paymentMethodId");
            if (paymentMethodId == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "Payment method ID is required"));
            }

            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            // Create or get Stripe customer
            String customerId = user.getStripeCustomerId();
            if (customerId == null) {
                customerId = stripeService.createCustomer(user.getEmail(), user.getUsername());
                user.setStripeCustomerId(customerId);
                userRepository.save(user);
            }

            // Check if card already exists
            Optional<Card> existingCard = cardRepository.findByStripePaymentMethodId(paymentMethodId);
            if (existingCard.isPresent()) {
                return ResponseEntity.badRequest().body(Map.of("error", "This card is already attached"));
            }

            // Attach payment method to customer
            stripeService.attachPaymentMethodToCustomer(paymentMethodId, customerId);

            // Save to local database
            PaymentMethod stripePaymentMethod = PaymentMethod.retrieve(paymentMethodId);

            Card card = new Card();
            card.setUser(user);
            card.setStripePaymentMethodId(paymentMethodId);
            card.setBrand(stripePaymentMethod.getCard().getBrand());
            card.setLast4(stripePaymentMethod.getCard().getLast4());
            card.setExpMonth(stripePaymentMethod.getCard().getExpMonth().intValue());
            card.setExpYear(stripePaymentMethod.getCard().getExpYear().intValue());

            // Set as default if this is the first card
            List<Card> userCards = cardRepository.findByUserId(user.getId());
            boolean isFirstCard = userCards.isEmpty();
            card.setIsDefault(isFirstCard);

            // If setting as default, unset other defaults
            if (isFirstCard) {
                card.setIsDefault(true);
                user.setStripePaymentMethodId(paymentMethodId);
                userRepository.save(user);
            }

            cardRepository.save(card);

            return ResponseEntity.ok().body(Map.of("message", "Payment method attached successfully"));

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @PostMapping("/test-charge")
    public ResponseEntity<?> testCharge(@RequestBody Map<String, Object> request, Principal principal) {
        try {
            String paymentMethodId = (String) request.get("paymentMethodId");
            Long amount = ((Number) request.get("amount")).longValue();
            String currency = (String) request.get("currency");

            User user = userRepository.findByEmail(principal.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            if (user.getStripeCustomerId() == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "User has no Stripe customer"));
            }

            boolean success = stripeService.chargeCustomer(
                    user.getStripeCustomerId(),
                    paymentMethodId,
                    currency,
                    amount);

            if (success) {
                return ResponseEntity.ok().body(Map.of("message", "Charge successful"));
            } else {
                return ResponseEntity.badRequest().body(Map.of("error", "Charge failed"));
            }

        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}