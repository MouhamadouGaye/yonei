package com.mgaye.yonei.entity;

import java.time.Instant;
import java.time.YearMonth;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Card {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, unique = true)
    private String stripePaymentMethodId;

    private String brand; // "visa", "mastercard", "amex"
    private String last4; // "4242"
    private Integer expMonth; // 12
    private Integer expYear; // 2028

    @Column(nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    @Column(name = "is_active")
    private Boolean isActive = true;

    // Helper method for display
    public String getDisplayName() {
        return String.format("%s ****%s", brand, last4);
    }

    public boolean isExpired() {
        YearMonth now = YearMonth.now();
        YearMonth expiry = YearMonth.of(expYear, expMonth);
        return expiry.isBefore(now);
    }
}