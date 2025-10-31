package com.mgaye.yonei.repository;

import com.mgaye.yonei.entity.TransactionEntry;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TransactionEntryRepository extends JpaRepository<TransactionEntry, Long> {
    Optional<TransactionEntry> findFirstByUserIdAndPrevEntryIdIsNull(Long userId); // head

    Optional<TransactionEntry> findFirstByUserIdAndNextEntryIdIsNull(Long userId); // tail

    // More simple and faster when not using linked-list
    List<TransactionEntry> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

}
