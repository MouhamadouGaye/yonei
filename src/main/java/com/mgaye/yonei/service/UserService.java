// package com.mgaye.moneytransfer.service;

// import com.mgaye.moneytransfer.entity.Card;
// import com.mgaye.moneytransfer.entity.User;
// import com.mgaye.moneytransfer.repository.CardRepository;
// import com.mgaye.moneytransfer.repository.UserRepository;
// import com.stripe.exception.StripeException;
// import com.stripe.model.Customer;
// import com.stripe.model.PaymentMethod;
// import com.stripe.param.CustomerCreateParams;
// import com.stripe.param.PaymentMethodAttachParams;

// import jakarta.transaction.Transactional;

// import org.springframework.security.core.userdetails.UserDetails;
// import org.springframework.security.core.userdetails.UsernameNotFoundException;
// import org.springframework.security.crypto.password.PasswordEncoder;
// import org.springframework.security.core.userdetails.UserDetailsService;

// import org.springframework.stereotype.Service;

// import java.time.Instant;
// import java.util.Optional;
// import java.math.BigDecimal;

// @Service
// public class UserService implements UserDetailsService {

//     private final UserRepository userRepository;
//     private final CardRepository cardRepository;
//     private final PasswordEncoder passwordEncoder;

//     public UserService(UserRepository userRepository, CardRepository carRepository, PasswordEncoder passwordEncoder) {
//         this.userRepository = userRepository;
//         this.passwordEncoder = passwordEncoder;
//         this.cardRepository = carRepository;
//     }

//     public boolean checkCredentials(String email, String rawPassword) {
//         return userRepository.findByEmail(email)
//                 .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
//                 .orElse(false);
//     }

//     public User getByEmail(String email) {
//         return userRepository.findByEmail(email)
//                 .orElseThrow(() -> new RuntimeException("User not found"));
//     }

//     public User createUser(String username, String email, String rawPassword, String phoneNumber) {
//         User user = User.builder()
//                 .username(username)
//                 .email(email)
//                 .password(passwordEncoder.encode(rawPassword))
//                 .phoneNumber(phoneNumber)
//                 .balance(BigDecimal.ZERO)
//                 .createdAt(Instant.now())
//                 .build();
//         return userRepository.save(user);
//     }

//     public Optional<User> findByEmail(String email) {
//         return userRepository.findByEmail(email);
//     }

//     public Optional<User> findById(Long id) {
//         return userRepository.findById(id);
//     }

//     @Override
//     public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
//         User user = userRepository.findByEmail(email)
//                 .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

//         return org.springframework.security.core.userdetails.User
//                 .withUsername(user.getEmail())
//                 .password(user.getPassword())
//                 .authorities("USER")
//                 .build();
//     }

//     /**
//      * Attach a Stripe PaymentMethod to a user and store its details locally.
//      * This ensures we can reuse the same card without asking the user again.
//      */
//     // @Transactional
//     // public void attachPaymentMethod(Long userId, String paymentMethodId) throws
//     // StripeException {
//     // User user = userRepository.findById(userId)
//     // .orElseThrow(() -> new RuntimeException("User not found"));

//     // // 1️⃣ Ensure Stripe Customer exists
//     // if (user.getStripeCustomerId() == null) {
//     // Customer customer = Customer.create(
//     // CustomerCreateParams.builder()
//     // .setEmail(user.getEmail())
//     // .build());
//     // user.setStripeCustomerId(customer.getId());
//     // userRepository.save(user);
//     // }

//     // // 2️⃣ Retrieve PaymentMethod
//     // PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
//     // if (pm.getCard() == null) {
//     // throw new IllegalStateException("Provided PaymentMethod is not a card");
//     // }

//     // // 3️⃣ Attach to Customer (so it can be reused!)
//     // pm.attach(PaymentMethodAttachParams.builder()
//     // .setCustomer(user.getStripeCustomerId())
//     // .build());

//     // // 4️⃣ Check if already stored in DB
//     // Optional<Card> existingCard =
//     // cardRepository.findByStripePaymentMethodId(paymentMethodId);
//     // if (existingCard.isPresent()) {
//     // return;
//     // }

//     // // 5️⃣ Save card in DB
//     // Card card = new Card();
//     // card.setUser(user);
//     // card.setStripePaymentMethodId(pm.getId());
//     // card.setBrand(pm.getCard().getBrand());
//     // card.setLast4(pm.getCard().getLast4());
//     // card.setExpMonth(pm.getCard().getExpMonth() != null ?
//     // pm.getCard().getExpMonth().intValue() : null);
//     // card.setExpYear(pm.getCard().getExpYear() != null ?
//     // pm.getCard().getExpYear().intValue() : null);

//     // boolean isFirstCard = cardRepository.findByUserId(userId).isEmpty();
//     // card.setIsDefault(isFirstCard);

//     // cardRepository.save(card);

//     // // 6️⃣ Optionally mark default in User
//     // if (isFirstCard || user.getStripePaymentMethodId() == null) {
//     // user.setStripePaymentMethodId(pm.getId());
//     // userRepository.save(user);
//     // }
//     // }

//     @Transactional
//     public void attachPaymentMethod(Long userId, String paymentMethodId) throws StripeException {
//         User user = userRepository.findById(userId)
//                 .orElseThrow(() -> new RuntimeException("User not found"));

//         // 0️⃣ Ensure user has a Stripe Customer
//         if (user.getStripeCustomerId() == null) {
//             CustomerCreateParams customerParams = CustomerCreateParams.builder()
//                     .setEmail(user.getEmail())
//                     .setName(user.getUsername())
//                     .build();
//             Customer customer = Customer.create(customerParams);
//             user.setStripeCustomerId(customer.getId());
//             userRepository.save(user);
//         }

//         // 1️⃣ Attach PaymentMethod to Stripe Customer
//         PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
//         pm.attach(PaymentMethodAttachParams.builder()
//                 .setCustomer(user.getStripeCustomerId())
//                 .build());

//         // 2️⃣ Save card info locally
//         Optional<Card> existingCard = cardRepository.findByStripePaymentMethodId(paymentMethodId);

//         if (existingCard.isPresent()) {
//             return; // already stored, nothing to do
//         }

//         Card card = new Card();
//         card.setUser(user);
//         card.setStripePaymentMethodId(pm.getId());
//         card.setBrand(pm.getCard().getBrand());
//         card.setLast4(pm.getCard().getLast4());
//         card.setExpMonth(pm.getCard().getExpMonth().intValue());
//         card.setExpYear(pm.getCard().getExpYear().intValue());

//         boolean isFirstCard = cardRepository.findByUserId(userId).isEmpty();
//         card.setIsDefault(isFirstCard);
//         cardRepository.save(card);

//         if (isFirstCard || user.getStripePaymentMethodId() == null) {
//             user.setStripePaymentMethodId(pm.getId());
//             userRepository.save(user);
//         }
//     }

// }

package com.mgaye.yonei.service;

import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.dto.CountryCodeEntry;
import com.mgaye.yonei.entity.Card;
import com.mgaye.yonei.repository.UserRepository;
import com.mgaye.yonei.repository.CardRepository;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.PaymentMethod;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.PaymentMethodAttachParams;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

@Service
public class UserService implements UserDetailsService {

    private final UserRepository userRepository;
    private final CardRepository cardRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final AuditService auditService;
    private final CountryCodeService countryCodeService;

    public UserService(UserRepository userRepository, CardRepository cardRepository,
            PasswordEncoder passwordEncoder, EmailService emailService,
            AuditService auditService, CountryCodeService countryCodeService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.cardRepository = cardRepository;
        this.emailService = emailService;
        this.auditService = auditService;
        this.countryCodeService = countryCodeService;
    }

    public User verifyEmail(String token) {
        User user = userRepository.findByEmailVerificationToken(token)
                .orElseThrow(() -> new RuntimeException("Invalid verification token"));

        if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setTokenExpiry(null);

        return userRepository.save(user);
    }

    public boolean isEmailVerified(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        return user.isEmailVerified();
    }

    /**
     * Verify user account using verification token
     */
    @Transactional
    public void verifyAccount(String verificationToken) {
        User user = userRepository.findByEmailVerificationToken(verificationToken)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        user.setEmailVerified(true);
        user.setEmailVerificationToken(null);
        user.setTokenExpiry(null);
        userRepository.save(user);

        auditService.logSecurityEvent(user.getId(), "ACCOUNT_VERIFIED",
                "Email verification completed");
    }

    /**
     * Generate password reset token for user
     */
    @Transactional
    public String generatePasswordResetToken(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        String resetToken = generateVerificationToken();
        user.setEmailVerificationToken(resetToken);
        user.setTokenExpiry(Instant.now().plusSeconds(60 * 60)); // 1 hour expiry

        userRepository.save(user);

        auditService.logSecurityEvent(userId, "PASSWORD_RESET_TOKEN_GENERATED",
                "Password reset token created");

        return resetToken;
    }

    /**
     * Reset password using reset token
     */
    @Transactional
    public void resetPassword(String resetToken, String newPassword) {
        User user = userRepository.findByEmailVerificationToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Invalid or expired reset token"));

        if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("Password reset token has expired");
        }

        // Validate new password strength
        if (!isValidPassword(newPassword)) {
            throw new RuntimeException("New password does not meet security requirements");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setEmailVerificationToken(null);
        user.setTokenExpiry(null);
        user.setLastPasswordChange(Instant.now());

        userRepository.save(user);

        auditService.logSecurityEvent(user.getId(), "PASSWORD_RESET_COMPLETED",
                "Password reset via token");
    }

    /**
     * Get user by reset token
     */
    public User getUserByResetToken(String resetToken) {
        return userRepository.findByEmailVerificationToken(resetToken)
                .orElseThrow(() -> new RuntimeException("Invalid reset token"));
    }

    /**
     * Enhanced user creation with email verification
     */
    // @Transactional
    // public User createUser(String username, String email, String rawPassword,
    // String phoneNumber) {
    // // Validate password strength during registration
    // if (!isValidPassword(rawPassword)) {
    // throw new RuntimeException("Password does not meet security requirements");
    // }

    // // Check if email already exists
    // if (userRepository.existsByEmail(email)) {
    // throw new RuntimeException("Email already registered");
    // }

    // // Generate email verification token
    // String verificationToken = generateVerificationToken();

    // User user = User.builder()
    // .name(username)
    // .email(email.toLowerCase().trim())
    // .password(passwordEncoder.encode(rawPassword))
    // .phoneNumber(phoneNumber)
    // .balance(BigDecimal.ZERO)
    // .createdAt(Instant.now())
    // .emailVerificationToken(verificationToken)
    // .tokenExpiry(Instant.now().plusSeconds(24 * 60 * 60)) // 24 hours
    // .emailVerified(false)
    // .failedLoginAttempts(0)
    // .accountLocked(false)
    // .build();

    // User savedUser = userRepository.save(user);

    // // Log registration event
    // auditService.logSecurityEvent(savedUser.getId(), "USER_REGISTERED",
    // "New user registration with email: " + email);

    // return savedUser;
    // }

    public User createUser(String username, String email, String rawPassword, String phoneNumber) {
        // Validate password strength during registration
        if (!isValidPassword(rawPassword)) {
            throw new RuntimeException("Password does not meet security requirements");
        }

        // Check if email already exists
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already registered");
        }

        // 🔥 Determine currency based on phone number
        String currency = countryCodeService.getCurrencyByPhoneNumber(phoneNumber);
        String countryCode = getCountryCodeFromPhoneNumber(phoneNumber);

        System.out.println("🌍 Setting currency: " + currency + " for phone: " + phoneNumber);

        // Generate email verification token
        String verificationToken = generateVerificationToken();

        User user = User.builder()
                .name(username)
                .email(email.toLowerCase().trim())
                .password(passwordEncoder.encode(rawPassword))
                .phoneNumber(phoneNumber)
                .balance(BigDecimal.ZERO)
                .createdAt(Instant.now())
                .emailVerificationToken(verificationToken)
                .tokenExpiry(Instant.now().plusSeconds(24 * 60 * 60)) // 24 hours
                .emailVerified(false)
                .failedLoginAttempts(0)
                .accountLocked(false)
                .currency(currency) // 🔥 Set the currency
                .countryCode(countryCode) // 🔥 Set country code if you have this field
                .build();

        User savedUser = userRepository.save(user);

        // Log registration event
        auditService.logSecurityEvent(savedUser.getId(), "USER_REGISTERED",
                "New user registration with email: " + email + ", currency: " + currency);

        return savedUser;
    }

    // 🔥 Helper method to get country code
    private String getCountryCodeFromPhoneNumber(String phoneNumber) {
        try {
            CountryCodeEntry country = countryCodeService.getCountryByPhoneNumber(phoneNumber);
            return country != null ? country.getCode() : "INTL";
        } catch (Exception e) {
            System.err.println("❌ Error getting country code for phone: " + phoneNumber + " - " + e.getMessage());
            return "INTL";
        }
    }

    /**
     * Enhanced password change with additional security
     */
    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Verify current password
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            // Log failed attempt
            auditService.logFailedAttempt(userId, "PASSWORD_CHANGE_FAILED",
                    "Incorrect current password provided", "N/A", "API");
            throw new RuntimeException("Current password is incorrect");
        }

        // Check if new password is same as current
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            throw new RuntimeException("New password cannot be the same as current password");
        }

        // Validate new password strength
        if (!isValidPassword(newPassword)) {
            throw new RuntimeException("New password does not meet security requirements");
        }

        // Update password
        user.setPassword(passwordEncoder.encode(newPassword));
        user.setLastPasswordChange(Instant.now());
        userRepository.save(user);

        // Send password change notification
        emailService.sendPasswordChangedEmail(user.getEmail(), user.getName());
        emailService.sendPasswordChangeNotification(user.getEmail(), user.getName());

        auditService.logSecurityEvent(userId, "PASSWORD_CHANGED",
                "Password successfully updated");
    }

    /**
     * Handle failed login attempts and account locking
     */
    @Transactional
    public void handleFailedLogin(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            int failedAttempts = user.getFailedLoginAttempts() != null ? user.getFailedLoginAttempts() : 0;
            failedAttempts++;

            user.setFailedLoginAttempts(failedAttempts);

            // Lock account after 5 failed attempts
            if (failedAttempts >= 5) {
                user.setAccountLocked(true);
                auditService.logSecurityEvent(user.getId(), "ACCOUNT_LOCKED",
                        "Account locked due to multiple failed login attempts");

                // Send security alert
                emailService.sendSuspiciousActivityAlert(
                        user.getEmail(),
                        user.getUsername(),
                        "Account locked due to multiple failed login attempts");
            }

            userRepository.save(user);

            auditService.logFailedAttempt(user.getId(), "LOGIN_FAILED",
                    "Failed login attempt #" + failedAttempts, "N/A", "API");
        });
    }

    /**
     * Reset failed login attempts on successful login
     */
    @Transactional
    public void resetFailedLoginAttempts(String email) {
        userRepository.findByEmail(email).ifPresent(user -> {
            if (user.getFailedLoginAttempts() != null && user.getFailedLoginAttempts() > 0) {
                user.setFailedLoginAttempts(0);
                userRepository.save(user);
            }
        });
    }

    /**
     * Unlock user account (admin function)
     */
    @Transactional
    public void unlockAccount(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setAccountLocked(false);
        user.setFailedLoginAttempts(0);
        userRepository.save(user);

        auditService.logSecurityEvent(userId, "ACCOUNT_UNLOCKED",
                "Account manually unlocked");
    }

    // Your existing methods remain the same...
    public boolean verifyPassword(Long userId, String rawPassword) {
        return userRepository.findById(userId)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    @Transactional
    public void initiateEmailChange(Long userId, String newEmail) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check if new email is already taken
        if (userRepository.findByEmail(newEmail).isPresent()) {
            throw new RuntimeException("Email already in use");
        }

        // Validate email format
        if (!isValidEmail(newEmail)) {
            throw new RuntimeException("Invalid email format");
        }

        // Generate email verification token
        String verificationToken = generateVerificationToken();

        // Store the pending email change
        user.setPendingEmail(newEmail);
        user.setEmailVerificationToken(verificationToken);
        user.setTokenExpiry(Instant.now().plusSeconds(24 * 60 * 60)); // 24 hours expiry

        userRepository.save(user);

        // Send verification email
        emailService.sendEmailChangeVerification(newEmail, verificationToken, user.getUsername());

        auditService.logSecurityEvent(userId, "EMAIL_CHANGE_INITIATED",
                "Email change requested to: " + newEmail);
    }

    @Transactional
    public void confirmEmailChange(String verificationToken) {
        User user = userRepository.findByEmailVerificationToken(verificationToken)
                .orElseThrow(() -> new RuntimeException("Invalid or expired verification token"));

        if (user.getTokenExpiry() != null && user.getTokenExpiry().isBefore(Instant.now())) {
            throw new RuntimeException("Verification token has expired");
        }

        if (user.getPendingEmail() == null) {
            throw new RuntimeException("No pending email change found");
        }

        // Update email
        String oldEmail = user.getEmail();
        user.setEmail(user.getPendingEmail());
        user.setPendingEmail(null);
        user.setEmailVerificationToken(null);
        user.setTokenExpiry(null);
        user.setEmailVerified(true);

        userRepository.save(user);

        // Send confirmation email to new address
        emailService.sendEmailChangeConfirmation(user.getEmail(), oldEmail, user.getUsername());

        auditService.logSecurityEvent(user.getId(), "EMAIL_CHANGE_COMPLETED",
                "Email successfully changed from " + oldEmail + " to " + user.getEmail());
    }

    @Transactional
    public User updateUserProfile(Long userId, String username, String phoneNumber) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (username != null && !username.trim().isEmpty()) {
            user.setName(username.trim());
        }

        if (phoneNumber != null && !phoneNumber.trim().isEmpty()) {
            if (!isValidPhoneNumber(phoneNumber)) {
                throw new RuntimeException("Invalid phone number format");
            }
            user.setPhoneNumber(phoneNumber.trim());
        }

        return userRepository.save(user);
    }

    // Validation methods
    private boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@(.+)$");
    }

    private boolean isValidPhoneNumber(String phoneNumber) {
        return phoneNumber != null && phoneNumber.matches("^[+]?[0-9]{10,15}$");
    }

    private boolean isValidPassword(String password) {
        // At least 8 chars, 1 uppercase, 1 lowercase, 1 number, 1 special char
        return password != null
                && password.matches("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$");
    }

    private String generateVerificationToken() {
        return java.util.UUID.randomUUID().toString();
    }

    public boolean checkCredentials(String email, String rawPassword) {
        return userRepository.findByEmail(email)
                .map(user -> passwordEncoder.matches(rawPassword, user.getPassword()))
                .orElse(false);
    }

    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Long id) {
        return userRepository.findById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + email));

        // Check if account is locked
        if (Boolean.TRUE.equals(user.getAccountLocked())) {
            throw new UsernameNotFoundException("Account is locked: " + email);
        }

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities("USER")
                .accountLocked(Boolean.TRUE.equals(user.getAccountLocked()))
                .build();
    }

    @Transactional
    public void attachPaymentMethod(Long userId, String paymentMethodId) throws StripeException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 0️⃣ Ensure user has a Stripe Customer
        if (user.getStripeCustomerId() == null) {
            CustomerCreateParams customerParams = CustomerCreateParams.builder()
                    .setEmail(user.getEmail())
                    .setName(user.getUsername())
                    .build();
            Customer customer = Customer.create(customerParams);
            user.setStripeCustomerId(customer.getId());
            userRepository.save(user);
        }

        // 1️⃣ Attach PaymentMethod to Stripe Customer
        PaymentMethod pm = PaymentMethod.retrieve(paymentMethodId);
        pm.attach(PaymentMethodAttachParams.builder()
                .setCustomer(user.getStripeCustomerId())
                .build());

        // 2️⃣ Save card info locally
        Optional<Card> existingCard = cardRepository.findByStripePaymentMethodId(paymentMethodId);

        if (existingCard.isPresent()) {
            return; // already stored, nothing to do
        }

        Card card = new Card();
        card.setUser(user);
        card.setStripePaymentMethodId(pm.getId());
        card.setBrand(pm.getCard().getBrand());
        card.setLast4(pm.getCard().getLast4());
        card.setExpMonth(pm.getCard().getExpMonth().intValue());
        card.setExpYear(pm.getCard().getExpYear().intValue());

        boolean isFirstCard = cardRepository.findByUserId(userId).isEmpty();
        card.setIsDefault(isFirstCard);
        cardRepository.save(card);

        if (isFirstCard || user.getStripePaymentMethodId() == null) {
            user.setStripePaymentMethodId(pm.getId());
            userRepository.save(user);
        }

        auditService.logSecurityEvent(userId, "PAYMENT_METHOD_ADDED",
                "Payment method attached: " + paymentMethodId);
    }
}