package com.mgaye.yonei.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

// @Entity
// @Table(name = "users")
// @Getter
// @Setter
// @NoArgsConstructor
// @AllArgsConstructor
// @Builder
// public class User {
//     @Id
//     @GeneratedValue(strategy = GenerationType.IDENTITY)
//     private Long id;

//     @Column(nullable = false, unique = true)
//     private String username;

//     @Column(nullable = false, unique = true)
//     private String email;

//     @Column(nullable = false)
//     private String password; // store hashed in real app

//     @Column(nullable = false, unique = true, length = 20)
//     private String phoneNumber; // 📱 Added phone number

//     @Column(nullable = false, precision = 19, scale = 4)
//     private BigDecimal balance = BigDecimal.ZERO;

//     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//     private List<Card> cards = new ArrayList<>();

//     @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
//     private List<TransactionEntry> transactionEntry; // this was add for strype payment

//     @Column(name = "stripe_customer_id", unique = true)
//     private String stripeCustomerId; // ✅ add this

//     @Column(name = "stripe_payment_method_id", unique = true)
//     private String stripePaymentMethodId; // this was add for strype payment service

//     // IDs to the transaction_entries table forming the user's linked list
//     @Column(name = "head_entry_id")
//     private Long headEntryId;

//     @Column(name = "tail_entry_id")
//     private Long tailEntryId;

//     @Column(name = "created_at", nullable = false, updatable = false)
//     private Instant createdAt = Instant.now();
// }

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User implements UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password; // store hashed in real app

    @Column(nullable = false, unique = true, length = 20)
    private String phoneNumber; // 📱 Added phone number

    @Column(name = "country_code", length = 5)
    private String countryCode;

    @Column(name = "currency", length = 3)
    private String currency; // ISO 4217 code (USD, EUR, etc.)

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance = BigDecimal.ZERO;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<Card> cards = new ArrayList<>();

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<TransactionEntry> transactionEntry; // this was add for strype payment

    @Column(name = "stripe_customer_id", unique = true)
    private String stripeCustomerId; // ✅ add this

    @Column(name = "stripe_payment_method_id", unique = true)
    private String stripePaymentMethodId; // this was add for strype payment service

    // IDs to the transaction_entries table forming the user's linked list
    @Column(name = "head_entry_id")
    private Long headEntryId;

    @Column(name = "tail_entry_id")
    private Long tailEntryId;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt = Instant.now();

    // ✅ ADD THESE MISSING FIELDS FOR EMAIL VERIFICATION
    @Column(name = "pending_email")
    private String pendingEmail;

    @Column(name = "email_verification_token")
    private String emailVerificationToken;

    @Column(name = "token_expiry")
    private Instant tokenExpiry;

    // ✅ ADD THESE FOR SECURITY/AUDIT
    @Column(name = "email_verified")
    private Boolean emailVerified = false;

    @Column(name = "last_password_change")
    private Instant lastPasswordChange;

    @Column(name = "account_locked")
    private Boolean accountLocked = false;

    @Column(name = "failed_login_attempts")
    private Integer failedLoginAttempts = 0;

    // ✅ Builder pattern customization for the new fields
    public static class UserBuilder {
        private String pendingEmail;
        private String emailVerificationToken;
        private Instant tokenExpiry;
        private Boolean emailVerified = false;
        private Instant lastPasswordChange;
        private Boolean accountLocked = false;
        private Integer failedLoginAttempts = 0;
    }

    public boolean isEmailVerified() {
        return emailVerified;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return List.of(new SimpleGrantedAuthority("ROLE_USER"));
    }

    @Override
    public String getPassword() {
        return this.password; // Make sure this field exists
    }

    @Override
    public String getUsername() {
        return this.email; // Or this.username - must match what's in JWT
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return !Boolean.TRUE.equals(accountLocked);
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return Boolean.TRUE.equals(emailVerified);
    }

}