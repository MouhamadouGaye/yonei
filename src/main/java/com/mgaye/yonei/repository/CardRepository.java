package com.mgaye.yonei.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.mgaye.yonei.entity.Card;

public interface CardRepository extends JpaRepository<Card, Long> {

    // 🔎 Find by Stripe PaymentMethod ID
    Optional<Card> findByStripePaymentMethodId(String stripePaymentMethodId);

    // 🔎 Get all cards of a user
    List<Card> findByUserId(Long userId);

    // 🔎 Find the default card for a user
    Optional<Card> findByUserIdAndIsDefaultTrue(Long userId);

    // Add this method to find default card by user
    @Query("SELECT c FROM Card c WHERE c.user.id = :userId AND c.isDefault = true")
    Optional<Card> findDefaultCardByUserId(@Param("userId") Long userId);

    void deleteByStripePaymentMethodId(String stripePaymentMethodId);

}
