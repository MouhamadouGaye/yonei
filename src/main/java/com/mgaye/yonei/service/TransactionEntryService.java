package com.mgaye.yonei.service;

import com.mgaye.yonei.entity.TransactionEntry;
import com.mgaye.yonei.entity.Transfer;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.TransactionEntryRepository;
import com.mgaye.yonei.repository.TransferRepository;
import com.mgaye.yonei.repository.UserRepository;

import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Service
public class TransactionEntryService {
    private final TransactionEntryRepository entryRepository;
    private final UserRepository userRepository;
    private final TransferRepository transferRepository;

    public TransactionEntryService(TransactionEntryRepository entryRepository, UserRepository userRepository,
            TransferRepository transferRepository) {
        this.entryRepository = entryRepository;
        this.userRepository = userRepository;
        this.transferRepository = transferRepository;
    }

    public Optional<TransactionEntry> findById(Long id) {
        return entryRepository.findById(id);
    }

    public Optional<TransactionEntry> findUserHead(Long userId) {
        return entryRepository.findFirstByUserIdAndPrevEntryIdIsNull(userId);
    }

    public Optional<TransactionEntry> findUserTail(Long userId) {
        return entryRepository.findFirstByUserIdAndNextEntryIdIsNull(userId);
    }

    /**
     * 
     * When you are not using linked-list,
     * fetch recent entries by createdAt desc
     */
    public List<TransactionEntry> findByUserIdOrderByCreatedAtDesc(Long userId, int limit) {
        return entryRepository.findByUserIdOrderByCreatedAtDesc(userId, PageRequest.of(0, limit));
    }

    /**
     * Add transaction entries for internal transfers (wallet or card).
     * For card payments, the sender's wallet stays unchanged but we still create a
     * debit entry with amount zero.
     */
    // @Transactional
    // public void addTransactionEntries(User fromUser, User toUser, Transfer
    // transfer, boolean fromCard) {
    // // Debit entry for sender
    // TransactionEntry debitEntry = TransactionEntry.builder()
    // .user(fromUser)
    // .transfer(transfer)
    // .amount(fromCard ? BigDecimal.ZERO : transfer.getAmount().negate())
    // .balanceAfter(fromUser.getBalance()) // card payments: balance unchanged
    // .createdAt(Instant.now())
    // .build();
    // entryRepository.save(debitEntry);

    // // Credit entry for recipient (internal user)
    // if (toUser != null) {
    // TransactionEntry creditEntry = TransactionEntry.builder()
    // .user(toUser)
    // .transfer(transfer)
    // .amount(transfer.getAmount())
    // .balanceAfter(toUser.getBalance())
    // .createdAt(Instant.now())
    // .build();
    // entryRepository.save(creditEntry);
    // }
    // }

    /**
     * Add a transaction entry for external transfers (beneficiary).
     * The sender's wallet is affected only if not using card.
     */
    @Transactional
    public void addTransactionEntry(User fromUser, Transfer transfer, boolean fromCard) {
        TransactionEntry entry = TransactionEntry.builder()
                .user(fromUser)
                .transfer(transfer)
                .amount(fromCard ? BigDecimal.ZERO : transfer.getAmount().negate())
                .balanceAfter(fromUser.getBalance())
                .createdAt(Instant.now())
                .build();
        entryRepository.save(entry);
    }

    @Transactional
    public void addTransactionEntries(User fromUser, User toUser, Transfer transfer, boolean fromCard) {
        // sender entry (negative amount if wallet, zero if card-only)
        if (!fromCard) {
            createEntry(fromUser.getId(), transfer.getId(), transfer.getAmount().negate());
        } else {
            // For card case, sender doesn’t lose from balance, but still log it if needed
            createEntry(fromUser.getId(), transfer.getId(), BigDecimal.ZERO);
        }

        // recipient entry
        if (toUser != null) {
            createEntry(toUser.getId(), transfer.getId(), transfer.getAmount());
        }
    }

    @Transactional
    public TransactionEntry createEntry(Long userId, Long transferId, BigDecimal amount) {
        // 1. Get the User
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));

        // 2. Get the Transfer (assuming you also need it)
        Transfer transfer = transferRepository.findById(transferId)
                .orElseThrow(() -> new EntityNotFoundException("Transfer not found"));

        // 3. Find the tail (latest transaction for this user)
        Optional<TransactionEntry> tailOpt = entryRepository.findFirstByUserIdAndNextEntryIdIsNull(userId);

        // 4. Build the new entry
        TransactionEntry newEntry = TransactionEntry.builder()
                .user(user)
                .transfer(transfer)
                .amount(amount)
                .createdAt(Instant.now())
                .balanceAfter(tailOpt.map(t -> t.getBalanceAfter().add(amount)).orElse(amount))
                .prevEntryId(tailOpt.map(TransactionEntry::getId).orElse(null))
                .nextEntryId(null)
                .build();

        // 5. Save the new entry
        final TransactionEntry savedEntry = entryRepository.save(newEntry);

        // 6. Update the old tail to point forward
        tailOpt.ifPresent(tail -> {
            tail.setNextEntryId(savedEntry.getId());
            entryRepository.save(tail);
        });

        return savedEntry;
    }

}
