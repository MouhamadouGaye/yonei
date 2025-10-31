// package com.mgaye.moneytransfer.service;

// import com.mgaye.moneytransfer.entity.*;
// import com.mgaye.moneytransfer.repository.*;
// import com.stripe.exception.StripeException;

// import jakarta.persistence.JoinColumn;
// import lombok.extern.slf4j.Slf4j;

// import org.springframework.stereotype.Service;
// import org.springframework.transaction.annotation.Transactional;
// import org.springframework.web.server.ResponseStatusException;

// import java.math.BigDecimal;
// import java.time.Instant;
// import java.util.List;
// import java.util.Optional;

// // @Service
// // public class TransferService {
// //     private final UserRepository userRepository;
// //     private final TransferRepository transferRepository;
// //     private final TransactionEntryRepository entryRepository;
// //     private final BeneficiaryRepository beneficiaryRepository;

// //     public TransferService(UserRepository userRepository,
// //             TransferRepository transferRepository,
// //             TransactionEntryRepository entryRepository, BeneficiaryRepository beneficiaryRepository) {
// //         this.userRepository = userRepository;
// //         this.transferRepository = transferRepository;
// //         this.entryRepository = entryRepository;
// //         this.beneficiaryRepository = beneficiaryRepository;
// //     }

// //     public Optional<User> getUser(Long id) {
// //         return userRepository.findById(id);
// //     }

// //     public List<Transfer> getUserTransfers(Long userId) {
// //         return transferRepository.findByFromUser_IdOrToUser_Id(userId, userId);
// //     }

// //     @Transactional
// //     public Transfer createTransfer(Long fromUserId, Long toUserId, Long beneficiaryId, BigDecimal amount) {
// //         User fromUser = userRepository.findById(fromUserId)
// //                 .orElseThrow(() -> new RuntimeException("Sender not found"));

// //         if (fromUser.getBalance().compareTo(amount) < 0) {
// //             throw new RuntimeException("Insufficient balance");
// //         }

// //         Transfer.TransferBuilder transferBuilder = Transfer.builder()
// //                 .fromUser(fromUser)
// //                 .amount(amount)
// //                 .status(Transfer.TransferStatus.COMPLETED)
// //                 .createdAt(Instant.now());

// //         if (toUserId != null) {
// //             // Cas interne
// //             User toUser = userRepository.findById(toUserId)
// //                     .orElseThrow(() -> new RuntimeException("Recipient not found"));

// //             // debit / credit
// //             fromUser.setBalance(fromUser.getBalance().subtract(amount));
// //             toUser.setBalance(toUser.getBalance().add(amount));
// //             userRepository.save(fromUser);
// //             userRepository.save(toUser);

// //             Transfer transfer = transferBuilder.toUser(toUser).build();
// //             transferRepository.save(transfer);

// //             // entries
// //             addTransactionEntries(fromUser, toUser, transfer);

// //             return transfer;

// //         } else if (beneficiaryId != null) {
// //             // Cas externe
// //             Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
// //                     .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

// //             // debit uniquement
// //             fromUser.setBalance(fromUser.getBalance().subtract(amount));
// //             userRepository.save(fromUser);

// //             Transfer transfer = transferBuilder.beneficiary(beneficiary).build();
// //             transferRepository.save(transfer);

// //             // entry seulement pour le sender
// //             addTransactionEntry(fromUser, transfer);

// //             return transfer;
// //         } else {
// //             throw new IllegalArgumentException("Either toUserId or beneficiaryId must be provided");
// //         }
// //     }

// //     private void addTransactionEntries(User fromUser, User toUser, Transfer transfer) {
// //         TransactionEntry fromEntry = TransactionEntry.builder()
// //                 .user(fromUser)
// //                 .transfer(transfer)
// //                 .createdAt(Instant.now())
// //                 .prevEntryId(fromUser.getTailEntryId())
// //                 .build();
// //         entryRepository.save(fromEntry);

// //         TransactionEntry toEntry = TransactionEntry.builder()
// //                 .user(toUser)
// //                 .transfer(transfer)
// //                 .createdAt(Instant.now())
// //                 .prevEntryId(toUser.getTailEntryId())
// //                 .build();
// //         entryRepository.save(toEntry);

// //         updateUserPointers(fromUser, fromEntry);
// //         updateUserPointers(toUser, toEntry);
// //     }

// //     private void addTransactionEntry(User user, Transfer transfer) {
// //         TransactionEntry entry = TransactionEntry.builder()
// //                 .user(user)
// //                 .transfer(transfer)
// //                 .createdAt(Instant.now())
// //                 .prevEntryId(user.getTailEntryId())
// //                 .build();
// //         entryRepository.save(entry);
// //         updateUserPointers(user, entry);
// //     }

// //     private void updateUserPointers(User user, TransactionEntry entry) {
// //         user.setTailEntryId(entry.getId());
// //         if (user.getHeadEntryId() == null) {
// //             user.setHeadEntryId(entry.getId());
// //         }
// //         userRepository.save(user);
// //     }

// // }

// @Slf4j
// @Service
// public class TransferService {
//     private final UserRepository userRepository;
//     private final TransferRepository transferRepository;
//     private final CardRepository cardRepository;
//     private final TransactionEntryRepository entryRepository;
//     private final BeneficiaryRepository beneficiaryRepository;
//     private final PaymentService paymentService;
//     private final TransactionEntryService transactionEntryService;

//     public TransferService(UserRepository userRepository,
//             TransferRepository transferRepository,
//             CardRepository cardRepository,
//             TransactionEntryRepository entryRepository,
//             BeneficiaryRepository beneficiaryRepository, PaymentService paymentService,
//             TransactionEntryService transactionEntryService) {
//         this.userRepository = userRepository;
//         this.transferRepository = transferRepository;
//         this.cardRepository = cardRepository;
//         this.entryRepository = entryRepository;
//         this.beneficiaryRepository = beneficiaryRepository;
//         this.paymentService = paymentService;
//         this.transactionEntryService = transactionEntryService;
//     }

//     // @Transactional
//     // public Transfer createTransfer(Long fromUserId, Long toUserId, Long
//     // beneficiaryId, BigDecimal amount) {
//     // User fromUser = userRepository.findById(fromUserId)
//     // .orElseThrow(() -> new RuntimeException("Sender not found"));

//     // if (fromUser.getBalance().compareTo(amount) < 0) {
//     // throw new RuntimeException("Insufficient balance");
//     // }

//     // // 🔴 Conflict: insufficient funds
//     // if (fromUser.getBalance().compareTo(amount) < 0) {
//     // throw new IllegalStateException("Insufficient balance");
//     // }

//     // Transfer.TransferBuilder transferBuilder = Transfer.builder()
//     // .fromUser(fromUser)
//     // .amount(amount)
//     // .status(Transfer.TransferStatus.COMPLETED)
//     // .createdAt(Instant.now());

//     // if (toUserId != null) {
//     // // ✅ Cas 1 : Transfert interne (User → User)
//     // // User toUser = userRepository.findById(toUserId)
//     // // .orElseThrow(() -> new RuntimeException("Recipient not found"));

//     // // ✅ Case 1: Internal transfer (User → User)
//     // User toUser = userRepository.findById(toUserId)
//     // .orElseThrow(() -> new IllegalArgumentException("Recipient not found"));

//     // // Débit / Crédit
//     // fromUser.setBalance(fromUser.getBalance().subtract(amount));
//     // toUser.setBalance(toUser.getBalance().add(amount));
//     // userRepository.save(fromUser);
//     // userRepository.save(toUser);

//     // Transfer transfer = transferBuilder.toUser(toUser).build();
//     // transferRepository.save(transfer);

//     // // Créer les deux entries (expéditeur + destinataire)
//     // addTransactionEntries(fromUser, toUser, transfer);

//     // return transfer;

//     // } else if (beneficiaryId != null) {
//     // // ✅ Cas 2 : Transfert externe (User → Beneficiary)
//     // // Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
//     // // .orElseThrow(() -> new RuntimeException("Beneficiary not found"));
//     // Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
//     // .orElseThrow(() -> new IllegalArgumentException("Beneficiary not found"));

//     // // Débit uniquement
//     // fromUser.setBalance(fromUser.getBalance().subtract(amount));
//     // userRepository.save(fromUser);

//     // Transfer transfer = transferBuilder.beneficiary(beneficiary).build();
//     // transferRepository.save(transfer);

//     // // Créer une entry uniquement pour le sender
//     // addTransactionEntry(fromUser, transfer);

//     // return transfer;
//     // } else {
//     // throw new IllegalArgumentException("Either toUserId or beneficiaryId must be
//     // provided");
//     // }
//     // }

//     // @Transactional
//     // public Transfer createTransferWithStripe(
//     // Long fromUserId,
//     // Long toUserId,
//     // Long beneficiaryId,
//     // BigDecimal amount,
//     // boolean fromCard) {

//     // log.info("➡️ Starting transfer: fromUserId={}, toUserId={}, beneficiaryId={},
//     // amount={}, fromCard={}",
//     // fromUserId, toUserId, beneficiaryId, amount, fromCard);

//     // User fromUser = userRepository.findById(fromUserId)
//     // .orElseThrow(() -> new RuntimeException("Sender not found"));

//     // log.info("✅ Sender loaded: {}", fromUser.getEmail());

//     // if (fromCard) {
//     // log.info("💳 Processing card payment with Stripe methodId={}",
//     // fromUser.getStripePaymentMethodId());
//     // } else {
//     // log.info("👛 Processing wallet transfer, balance={}", fromUser.getBalance());
//     // }

//     // if (!fromCard) {
//     // // Wallet case → check balance
//     // if (fromUser.getBalance().compareTo(amount) < 0) {
//     // throw new IllegalStateException("Insufficient wallet balance");
//     // }
//     // } else {
//     // // Card case → Stripe charge
//     // String paymentMethodId = fromUser.getStripePaymentMethodId();

//     // if (paymentMethodId == null || paymentMethodId.isBlank()) {
//     // throw new IllegalStateException(
//     // "No Stripe payment method found for this user. Attach a card first.");
//     // }

//     // String currency = "usd"; // or fromUser.getCurrency()
//     // long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

//     // boolean charged;
//     // try {
//     // charged = paymentService.chargeCard(paymentMethodId, currency,
//     // amountInCents);
//     // } catch (StripeException e) {
//     // throw new RuntimeException("Stripe payment failed: " + e.getMessage(), e);
//     // }
//     // if (!charged) {
//     // throw new RuntimeException("Card payment failed");
//     // }
//     // }

//     // Transfer.TransferBuilder transferBuilder = Transfer.builder()
//     // .fromUser(fromUser)
//     // .amount(amount)
//     // .status(Transfer.TransferStatus.COMPLETED)
//     // .createdAt(Instant.now());

//     // if (toUserId != null) {

//     // // the end of the tes
//     // User toUser = userRepository.findById(toUserId)
//     // .orElseThrow(() -> new RuntimeException("Recipient not found"));

//     // if (!fromCard) {
//     // fromUser.setBalance(fromUser.getBalance().subtract(amount));
//     // toUser.setBalance(toUser.getBalance().add(amount));
//     // userRepository.save(fromUser);
//     // userRepository.save(toUser);
//     // } else {
//     // // ✅ Card payment → only credit the recipient
//     // toUser.setBalance(toUser.getBalance().add(amount));
//     // userRepository.save(toUser);
//     // }

//     // Transfer transfer = transferBuilder
//     // .toUser(toUser)
//     // .beneficiary(null)
//     // .build();

//     // transferRepository.save(transfer);

//     // // ✅ Add transaction en;ktries for both wallet and card
//     // transactionEntryService.addTransactionEntries(fromUser, toUser, transfer,
//     // fromCard);
//     // return transfer;

//     // } else if (beneficiaryId != null) {

//     // Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
//     // .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

//     // if (!fromCard) {
//     // fromUser.setBalance(fromUser.getBalance().subtract(amount));
//     // userRepository.save(fromUser);
//     // }
//     // // ✅ Card payment: nothing to subtract from wallet

//     // Transfer transfer = transferBuilder
//     // .toUser(null)
//     // .beneficiary(beneficiary)
//     // .build();

//     // transferRepository.save(transfer);

//     // // ✅ Add transaction entry (wallet ùaffected or card payment)
//     // transactionEntryService.addTransactionEntry(fromUser, transfer, fromCard);
//     // return transfer;

//     // } else {
//     // throw new IllegalArgumentException("Either toUserId or beneficiaryId must be
//     // provided");
//     // }
//     // }

//     @Transactional
//     public Transfer createTransferWithStripe(
//             Long fromUserId,
//             Long toUserId,
//             Long beneficiaryId,
//             BigDecimal amount,
//             boolean fromCard) {

//         // 1️⃣ Load sender
//         User fromUser = userRepository.findById(fromUserId)
//                 .orElseThrow(() -> new RuntimeException("Sender not found"));

//         // 2️⃣ Check wallet case (direct balance transfer)
//         if (!fromCard) {
//             if (fromUser.getBalance().compareTo(amount) < 0) {
//                 throw new IllegalStateException("Insufficient wallet balance");
//             }
//         } else {
//             // // 3️⃣ Card case → ensure a Stripe payment method exists
//             // String paymentMethodId = fromUser.getStripePaymentMethodId();
//             // if (paymentMethodId == null || paymentMethodId.isBlank()) {
//             // throw new IllegalStateException(
//             // "No Stripe payment method found for this user. Attach a card first.");
//             // }

//             // // Convert amount → cents
//             // String currency = "usd"; // you could also store per-user currency
//             // long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

//             // // 4️⃣ Call Stripe to attempt the charge
//             // boolean charged;
//             // try {
//             // charged = paymentService.chargeCard(paymentMethodId, currency,
//             // amountInCents);
//             // } catch (StripeException e) {
//             // throw new RuntimeException("Stripe payment failed: " + e.getMessage(), e);
//             // }

//             // // If charge not successful → stop here
//             // if (!charged) {
//             // throw new RuntimeException("Card payment failed or requires action.");
//             // }

//             Card defaultCard = cardRepository.findByUserIdAndIsDefaultTrue(fromUserId)
//                     .orElseThrow(() -> new IllegalStateException(
//                             "No default card found. Please attach a card first."));

//             String customerId = fromUser.getStripeCustomerId();
//             String paymentMethodId = defaultCard.getStripePaymentMethodId();
//             String currency = "usd";
//             long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

//             boolean charged;
//             try {
//                 charged = paymentService.chargeCard(
//                         customerId,
//                         paymentMethodId,
//                         currency,
//                         amountInCents);
//             } catch (StripeException e) {
//                 throw new RuntimeException("Stripe payment failed: " + e.getMessage(), e);
//             }

//             if (!charged) {
//                 throw new RuntimeException("Card payment failed or requires action.");
//             }
//         }

//         // 5️⃣ Build the base Transfer entity
//         Transfer.TransferBuilder transferBuilder = Transfer.builder()
//                 .fromUser(fromUser)
//                 .amount(amount)
//                 .status(Transfer.TransferStatus.COMPLETED) // only set if charge succeeded
//                 .createdAt(Instant.now());

//         // 6️⃣ Case: transfer to another user
//         if (toUserId != null) {
//             User toUser = userRepository.findById(toUserId)
//                     .orElseThrow(() -> new RuntimeException("Recipient not found"));

//             if (!fromCard) {
//                 // Wallet transfer → subtract and credit
//                 fromUser.setBalance(fromUser.getBalance().subtract(amount));
//                 toUser.setBalance(toUser.getBalance().add(amount));
//                 userRepository.save(fromUser);
//                 userRepository.save(toUser);
//             } else {
//                 // Card payment → only credit recipient
//                 toUser.setBalance(toUser.getBalance().add(amount));
//                 userRepository.save(toUser);
//             }

//             Transfer transfer = transferBuilder
//                     .toUser(toUser)
//                     .beneficiary(null)
//                     .build();

//             transferRepository.save(transfer);

//             // Create transaction entries (sender/recipient logs)
//             transactionEntryService.addTransactionEntries(fromUser, toUser, transfer, fromCard);

//             return transfer;
//         }

//         // 7️⃣ Case: transfer to an external beneficiary
//         else if (beneficiaryId != null) {
//             Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
//                     .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

//             if (!fromCard) {
//                 // Wallet → subtract balance
//                 fromUser.setBalance(fromUser.getBalance().subtract(amount));
//                 userRepository.save(fromUser);
//             }
//             // Card payment → nothing to subtract

//             Transfer transfer = transferBuilder
//                     .toUser(null)
//                     .beneficiary(beneficiary)
//                     .build();

//             transferRepository.save(transfer);

//             // Create transaction entry (single-side)
//             transactionEntryService.addTransactionEntry(fromUser, transfer, fromCard);

//             return transfer;
//         }

//         // 8️⃣ Invalid call
//         else {
//             throw new IllegalArgumentException("Either toUserId or beneficiaryId must be provided");
//         }
//     }

//     // @Transactional
//     // public Transfer createTransfer(
//     // Long fromUserId,
//     // Long toUserId,
//     // Long beneficiaryId,
//     // BigDecimal amount,
//     // boolean fromCard) {

//     // User fromUser = userRepository.findById(fromUserId)
//     // .orElseThrow(() -> new RuntimeException("Sender not found"));

//     // // Only check balance if NOT using card
//     // if (!fromCard && fromUser.getBalance().compareTo(amount) < 0) {
//     // throw new RuntimeException("Insufficient balance");
//     // }

//     // Transfer.TransferBuilder transferBuilder = Transfer.builder()
//     // .fromUser(fromUser)
//     // .amount(amount)
//     // .status(Transfer.TransferStatus.COMPLETED)
//     // .createdAt(Instant.now());

//     // if (toUserId != null) {
//     // // -------------------------
//     // // Internal transfer
//     // // -------------------------
//     // User toUser = userRepository.findById(toUserId)
//     // .orElseThrow(() -> new RuntimeException("Recipient not found"));

//     // if (!fromCard) {
//     // fromUser.setBalance(fromUser.getBalance().subtract(amount));
//     // toUser.setBalance(toUser.getBalance().add(amount));
//     // userRepository.save(fromUser);
//     // userRepository.save(toUser);
//     // }

//     // Transfer transfer = transferBuilder
//     // .toUser(toUser) // ✅ set internal recipient
//     // .beneficiary(null) // ✅ make sure beneficiary is null
//     // .build();

//     // transferRepository.save(transfer);

//     // if (!fromCard) {
//     // addTransactionEntries(fromUser, toUser, transfer);
//     // }

//     // return transfer;

//     // } else if (beneficiaryId != null) {
//     // // -------------------------
//     // // External transfer
//     // // -------------------------
//     // Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
//     // .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

//     // if (!fromCard) {
//     // fromUser.setBalance(fromUser.getBalance().subtract(amount));
//     // userRepository.save(fromUser);
//     // }

//     // Transfer transfer = transferBuilder
//     // .toUser(null) // ✅ clear toUser
//     // .beneficiary(beneficiary)
//     // .build();

//     // transferRepository.save(transfer);

//     // if (!fromCard) {
//     // addTransactionEntry(fromUser, transfer);
//     // }

//     // return transfer;

//     // } else {
//     // throw new IllegalArgumentException("Either toUserId or beneficiaryId must be
//     // provided");
//     // }
//     // }

//     public List<Transfer> getUserTransfers(Long userId) {
//         return transferRepository.findByFromUser_IdOrToUser_Id(userId, userId);
//     }

//     // 🔹 Crée une transaction entry pour un utilisateur
//     private void addTransactionEntry(User user, Transfer transfer) {
//         TransactionEntry entry = TransactionEntry.builder()
//                 .user(user)
//                 .transfer(transfer)
//                 .createdAt(Instant.now())
//                 .prevEntryId(user.getTailEntryId())
//                 .build();
//         entryRepository.save(entry);

//         // mise à jour du linked list
//         user.setTailEntryId(entry.getId());
//         if (user.getHeadEntryId() == null) {
//             user.setHeadEntryId(entry.getId());
//         }
//         userRepository.save(user);
//     }

//     // 🔹 Crée les deux entries (expéditeur + destinataire)
//     private void addTransactionEntries(User fromUser, User toUser, Transfer transfer) {
//         addTransactionEntry(fromUser, transfer);
//         addTransactionEntry(toUser, transfer);
//     }
// }

package com.mgaye.yonei.service;

import com.mgaye.yonei.entity.*;
import com.mgaye.yonei.repository.*;
import com.stripe.exception.StripeException;

import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

@Slf4j
@Service
public class TransferService {
    private final UserRepository userRepository;
    private final TransferRepository transferRepository;
    private final CardRepository cardRepository;
    private final TransactionEntryRepository entryRepository;
    private final BeneficiaryRepository beneficiaryRepository;
    private final PaymentService paymentService;
    private final TransactionEntryService transactionEntryService;
    private final EmailService emailService;
    private final AuditService auditService;

    public TransferService(UserRepository userRepository,
            TransferRepository transferRepository,
            CardRepository cardRepository,
            TransactionEntryRepository entryRepository,
            BeneficiaryRepository beneficiaryRepository,
            PaymentService paymentService,
            TransactionEntryService transactionEntryService,
            EmailService emailService,
            AuditService auditService) {
        this.userRepository = userRepository;
        this.transferRepository = transferRepository;
        this.cardRepository = cardRepository;
        this.entryRepository = entryRepository;
        this.beneficiaryRepository = beneficiaryRepository;
        this.paymentService = paymentService;
        this.transactionEntryService = transactionEntryService;
        this.emailService = emailService;
        this.auditService = auditService;
    }

    @Transactional
    public Transfer createTransferWithStripe(
            Long fromUserId,
            Long toUserId,
            Long beneficiaryId,
            BigDecimal amount,
            boolean fromCard) {

        // 1️⃣ Load sender
        User fromUser = userRepository.findById(fromUserId)
                .orElseThrow(() -> new RuntimeException("Sender not found"));

        // 2️⃣ Check wallet case (direct balance transfer)
        if (!fromCard) {
            if (fromUser.getBalance().compareTo(amount) < 0) {
                // Send transfer failed notification
                emailService.sendTransferFailedNotification(
                        fromUser.getEmail(),
                        fromUser.getUsername(),
                        "Insufficient wallet balance",
                        amount.toString(),
                        "USD");
                throw new IllegalStateException("Insufficient wallet balance");
            }
        } else {
            Card defaultCard = cardRepository.findByUserIdAndIsDefaultTrue(fromUserId)
                    .orElseThrow(() -> {
                        // Send notification about missing card
                        emailService.sendTransferFailedNotification(
                                fromUser.getEmail(),
                                fromUser.getUsername(),
                                "No default payment method found",
                                amount.toString(),
                                "USD");
                        return new IllegalStateException("No default card found. Please attach a card first.");
                    });

            String customerId = fromUser.getStripeCustomerId();
            String paymentMethodId = defaultCard.getStripePaymentMethodId();
            String currency = "usd";
            long amountInCents = amount.multiply(BigDecimal.valueOf(100)).longValue();

            boolean charged;
            try {
                charged = paymentService.chargeCard(
                        customerId,
                        paymentMethodId,
                        currency,
                        amountInCents);
            } catch (StripeException e) {
                // Send payment failure email
                emailService.sendTransferFailedNotification(
                        fromUser.getEmail(),
                        fromUser.getUsername(),
                        "Payment processing failed: " + e.getMessage(),
                        amount.toString(),
                        "USD");

                // Log audit event
                auditService.logFailedAttempt(fromUserId, "STRIPE_PAYMENT_FAILED",
                        "Stripe payment failed: " + e.getMessage(), "N/A", "Stripe API");

                throw new RuntimeException("Stripe payment failed: " + e.getMessage(), e);
            }

            if (!charged) {
                // Send payment failure email
                emailService.sendTransferFailedNotification(
                        fromUser.getEmail(),
                        fromUser.getUsername(),
                        "Card payment failed or requires additional action",
                        amount.toString(),
                        "USD");
                throw new RuntimeException("Card payment failed or requires action.");
            }
        }

        // 5️⃣ Build the base Transfer entity
        Transfer.TransferBuilder transferBuilder = Transfer.builder()
                .fromUser(fromUser)
                .amount(amount)
                .status(Transfer.TransferStatus.COMPLETED)
                .createdAt(Instant.now());

        Transfer transfer = null;

        try {
            // 6️⃣ Case: transfer to another user
            if (toUserId != null) {
                User toUser = userRepository.findById(toUserId)
                        .orElseThrow(() -> new RuntimeException("Recipient not found"));

                if (!fromCard) {
                    // Wallet transfer → subtract and credit
                    fromUser.setBalance(fromUser.getBalance().subtract(amount));
                    toUser.setBalance(toUser.getBalance().add(amount));
                    userRepository.save(fromUser);
                    userRepository.save(toUser);
                } else {
                    // Card payment → only credit recipient
                    toUser.setBalance(toUser.getBalance().add(amount));
                    userRepository.save(toUser);
                }

                transfer = transferBuilder
                        .toUser(toUser)
                        .beneficiary(null)
                        .build();

                transferRepository.save(transfer);

                // Create transaction entries (sender/recipient logs)
                transactionEntryService.addTransactionEntries(fromUser, toUser, transfer, fromCard);

                // ✅ SEND SUCCESS EMAILS
                sendTransferSuccessEmails(fromUser, toUser, transfer, fromCard);

                // ✅ LOG AUDIT EVENTS
                logTransferAuditEvents(fromUser, toUser, transfer, fromCard);

                return transfer;
            }

            // 7️⃣ Case: transfer to an external beneficiary
            else if (beneficiaryId != null) {
                Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                        .orElseThrow(() -> new RuntimeException("Beneficiary not found"));

                if (!fromCard) {
                    // Wallet → subtract balance
                    fromUser.setBalance(fromUser.getBalance().subtract(amount));
                    userRepository.save(fromUser);
                }
                // Card payment → nothing to subtract

                transfer = transferBuilder
                        .toUser(null)
                        .beneficiary(beneficiary)
                        .build();

                transferRepository.save(transfer);

                // Create transaction entry (single-side)
                transactionEntryService.addTransactionEntry(fromUser, transfer, fromCard);

                // ✅ SEND SUCCESS EMAIL TO SENDER ONLY
                sendBeneficiaryTransferSuccessEmail(fromUser, beneficiary, transfer, fromCard);

                // ✅ LOG AUDIT EVENT
                logBeneficiaryTransferAuditEvent(fromUser, beneficiary, transfer, fromCard);

                return transfer;
            }

            // 8️⃣ Invalid call
            else {
                throw new IllegalArgumentException("Either toUserId or beneficiaryId must be provided");
            }

        } catch (Exception e) {
            // Send failure notification if something goes wrong during transfer processing
            emailService.sendTransferFailedNotification(
                    fromUser.getEmail(),
                    fromUser.getUsername(),
                    "Transfer processing failed: " + e.getMessage(),
                    amount.toString(),
                    "USD");
            throw e;
        }
    }

    /**
     * Send success emails for user-to-user transfers
     */
    private void sendTransferSuccessEmails(User fromUser, User toUser, Transfer transfer, boolean fromCard) {
        try {
            // Send confirmation to sender
            emailService.sendTransferSentConfirmation(
                    fromUser.getEmail(),
                    fromUser.getUsername(),
                    toUser.getUsername(),
                    transfer.getAmount().toString(),
                    "USD",
                    transfer.getId().toString());

            // Send notification to recipient
            emailService.sendTransferReceivedNotification(
                    toUser.getEmail(),
                    toUser.getUsername(),
                    fromUser.getUsername(),
                    transfer.getAmount().toString(),
                    "USD",
                    transfer.getId().toString());

            // Send large transfer alert if applicable
            if (isLargeTransfer(transfer.getAmount())) {
                emailService.sendLargeTransferAlert(
                        fromUser.getEmail(),
                        fromUser.getUsername(),
                        transfer.getAmount().toString(),
                        "USD");
            }

        } catch (Exception e) {
            log.error("Failed to send transfer success emails for transfer {}: {}",
                    transfer.getId(), e.getMessage());
            // Don't throw exception - email failure shouldn't rollback transaction
        }
    }

    /**
     * Send success email for beneficiary transfers
     */
    private void sendBeneficiaryTransferSuccessEmail(User fromUser, Beneficiary beneficiary,
            Transfer transfer, boolean fromCard) {
        try {
            emailService.sendTransferSentConfirmation(
                    fromUser.getEmail(),
                    fromUser.getUsername(),
                    beneficiary.getFullName(),
                    transfer.getAmount().toString(),
                    "USD",
                    transfer.getId().toString());

            // Send large transfer alert if applicable
            if (isLargeTransfer(transfer.getAmount())) {
                emailService.sendLargeTransferAlert(
                        fromUser.getEmail(),
                        fromUser.getUsername(),
                        transfer.getAmount().toString(),
                        "USD");
            }

        } catch (Exception e) {
            log.error("Failed to send beneficiary transfer email for transfer {}: {}",
                    transfer.getId(), e.getMessage());
        }
    }

    /**
     * Log audit events for user-to-user transfers
     */
    private void logTransferAuditEvents(User fromUser, User toUser, Transfer transfer, boolean fromCard) {
        try {
            String paymentMethod = fromCard ? "CARD" : "WALLET";

            auditService.logTransactionEvent(
                    fromUser.getId(),
                    "TRANSFER_SENT",
                    String.format("Transfer to %s (%s)", toUser.getUsername(), toUser.getEmail()),
                    transfer.getId(),
                    transfer.getAmount(),
                    "USD");

            auditService.logTransactionEvent(
                    toUser.getId(),
                    "TRANSFER_RECEIVED",
                    String.format("Transfer from %s (%s)", fromUser.getUsername(), fromUser.getEmail()),
                    transfer.getId(),
                    transfer.getAmount(),
                    "USD");

            // Log security event for large transfers
            if (isLargeTransfer(transfer.getAmount())) {
                auditService.logSecurityEvent(
                        fromUser.getId(),
                        "LARGE_TRANSFER",
                        String.format("Large transfer of %s USD to %s",
                                transfer.getAmount(), toUser.getUsername()));
            }

        } catch (Exception e) {
            log.error("Failed to log audit events for transfer {}: {}",
                    transfer.getId(), e.getMessage());
        }
    }

    /**
     * Log audit events for beneficiary transfers
     */
    private void logBeneficiaryTransferAuditEvent(User fromUser, Beneficiary beneficiary,
            Transfer transfer, boolean fromCard) {
        try {
            String paymentMethod = fromCard ? "CARD" : "WALLET";

            auditService.logTransactionEvent(
                    fromUser.getId(),
                    "BENEFICIARY_TRANSFER",
                    String.format("Transfer to beneficiary %s (%s)",
                            beneficiary.getFullName(), beneficiary.getPhoneNumber()),
                    transfer.getId(),
                    transfer.getAmount(),
                    "USD");

            // Log security event for large transfers
            if (isLargeTransfer(transfer.getAmount())) {
                auditService.logSecurityEvent(
                        fromUser.getId(),
                        "LARGE_BENEFICIARY_TRANSFER",
                        String.format("Large transfer of %s USD to beneficiary %s",
                                transfer.getAmount(), beneficiary.getFullName()));
            }

        } catch (Exception e) {
            log.error("Failed to log audit events for beneficiary transfer {}: {}",
                    transfer.getId(), e.getMessage());
        }
    }

    /**
     * Check if transfer amount is considered large (for alerts)
     */
    private boolean isLargeTransfer(BigDecimal amount) {
        // Define your large transfer threshold (e.g., $1000)
        BigDecimal largeTransferThreshold = new BigDecimal("1000.00");
        return amount.compareTo(largeTransferThreshold) >= 0;
    }

    /**
     * Process a transfer that requires manual review
     */
    @Transactional
    public Transfer createTransferPendingReview(Long fromUserId, Long toUserId,
            Long beneficiaryId, BigDecimal amount,
            boolean fromCard, String reviewReason) {
        try {
            User fromUser = userRepository.findById(fromUserId)
                    .orElseThrow(() -> new RuntimeException("Sender not found"));

            // Create transfer with PENDING status
            Transfer transfer = Transfer.builder()
                    .fromUser(fromUser)
                    .amount(amount)
                    .status(Transfer.TransferStatus.PENDING_REVIEW)
                    .createdAt(Instant.now())
                    .build();

            if (toUserId != null) {
                User toUser = userRepository.findById(toUserId)
                        .orElseThrow(() -> new RuntimeException("Recipient not found"));
                transfer.setToUser(toUser);
            } else if (beneficiaryId != null) {
                Beneficiary beneficiary = beneficiaryRepository.findById(beneficiaryId)
                        .orElseThrow(() -> new RuntimeException("Beneficiary not found"));
                transfer.setBeneficiary(beneficiary);
            }

            transferRepository.save(transfer);

            // Send pending review notification
            emailService.sendTransferPendingReview(
                    fromUser.getEmail(),
                    fromUser.getUsername(),
                    amount.toString(),
                    "USD",
                    transfer.getId().toString());

            // Log audit event
            auditService.logTransactionEvent(
                    fromUser.getId(),
                    "TRANSFER_PENDING_REVIEW",
                    String.format("Transfer pending review: %s", reviewReason),
                    transfer.getId(),
                    amount,
                    "USD");

            return transfer;

        } catch (Exception e) {
            log.error("Failed to create pending review transfer: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Approve a pending transfer
     */
    @Transactional
    public Transfer approvePendingTransfer(Long transferId) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (transfer.getStatus() != Transfer.TransferStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Transfer is not in pending review status");
        }

        try {
            // Process the actual transfer (similar to createTransferWithStripe but without
            // card check)
            User fromUser = transfer.getFromUser();

            if (transfer.getToUser() != null) {
                // User-to-user transfer
                User toUser = transfer.getToUser();

                // Deduct from sender and add to recipient
                fromUser.setBalance(fromUser.getBalance().subtract(transfer.getAmount()));
                toUser.setBalance(toUser.getBalance().add(transfer.getAmount()));
                userRepository.save(fromUser);
                userRepository.save(toUser);

                // Send success emails
                sendTransferSuccessEmails(fromUser, toUser, transfer, false);

            } else if (transfer.getBeneficiary() != null) {
                // Beneficiary transfer - only deduct from sender
                fromUser.setBalance(fromUser.getBalance().subtract(transfer.getAmount()));
                userRepository.save(fromUser);

                sendBeneficiaryTransferSuccessEmail(fromUser, transfer.getBeneficiary(), transfer, false);
            }

            // Update transfer status
            transfer.setStatus(Transfer.TransferStatus.COMPLETED);
            transferRepository.save(transfer);

            // Log approval audit event
            auditService.logSecurityEvent(
                    fromUser.getId(),
                    "TRANSFER_APPROVED",
                    String.format("Pending transfer %s approved and processed", transferId));

            return transfer;

        } catch (Exception e) {
            log.error("Failed to approve pending transfer {}: {}", transferId, e.getMessage());
            throw e;
        }
    }

    /**
     * Reject a pending transfer
     */
    @Transactional
    public Transfer rejectPendingTransfer(Long transferId, String rejectionReason) {
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new RuntimeException("Transfer not found"));

        if (transfer.getStatus() != Transfer.TransferStatus.PENDING_REVIEW) {
            throw new IllegalStateException("Transfer is not in pending review status");
        }

        transfer.setStatus(Transfer.TransferStatus.REJECTED);
        transferRepository.save(transfer);

        // Send rejection notification
        emailService.sendTransferFailedNotification(
                transfer.getFromUser().getEmail(),
                transfer.getFromUser().getUsername(),
                "Transfer rejected: " + rejectionReason,
                transfer.getAmount().toString(),
                "USD");

        // Log rejection audit event
        auditService.logSecurityEvent(
                transfer.getFromUser().getId(),
                "TRANSFER_REJECTED",
                String.format("Transfer %s rejected: %s", transferId, rejectionReason));

        return transfer;
    }

    public List<Transfer> getUserTransfers(Long userId) {
        return transferRepository.findByFromUser_IdOrToUser_Id(userId, userId);
    }

    // 🔹 Crée une transaction entry pour un utilisateur
    private void addTransactionEntry(User user, Transfer transfer) {
        TransactionEntry entry = TransactionEntry.builder()
                .user(user)
                .transfer(transfer)
                .createdAt(Instant.now())
                .prevEntryId(user.getTailEntryId())
                .build();
        entryRepository.save(entry);

        // mise à jour du linked list
        user.setTailEntryId(entry.getId());
        if (user.getHeadEntryId() == null) {
            user.setHeadEntryId(entry.getId());
        }
        userRepository.save(user);
    }

    // 🔹 Crée les deux entries (expéditeur + destinataire)
    private void addTransactionEntries(User fromUser, User toUser, Transfer transfer) {
        addTransactionEntry(fromUser, transfer);
        addTransactionEntry(toUser, transfer);
    }
}