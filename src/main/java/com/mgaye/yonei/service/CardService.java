package com.mgaye.yonei.service;

import org.springframework.stereotype.Service;

import com.mgaye.yonei.entity.Card;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.CardRepository;
import com.stripe.Stripe;
import com.stripe.model.PaymentMethod;
import com.stripe.exception.StripeException;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final StripeService stripeService;

    public CardService(CardRepository cardRepository, StripeService stripeService) {
        this.cardRepository = cardRepository;
        this.stripeService = stripeService;
    }

    public Card addCard(User user, String stripePaymentMethodId) throws StripeException {
        // Retrieve card details from Stripe
        PaymentMethod paymentMethod = stripeService.getPaymentMethodFromIntent(stripePaymentMethodId);

        Card card = Card.builder()
                .user(user)
                .stripePaymentMethodId(stripePaymentMethodId)
                .brand(paymentMethod.getCard().getBrand())
                .last4(paymentMethod.getCard().getLast4())
                .expMonth(paymentMethod.getCard().getExpMonth().intValue())
                .expYear(paymentMethod.getCard().getExpYear().intValue())
                .isDefault(!hasAnyCards(user)) // First card becomes default
                .build();

        return cardRepository.save(card);
    }

    private boolean hasAnyCards(User user) {
        return cardRepository.countByUser(user) > 0;
    }
}