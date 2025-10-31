package com.mgaye.yonei.controller;

import com.mgaye.yonei.dto.TransactionEntryDto;
import com.mgaye.yonei.dto.request.TransactionEntryRequest;
import com.mgaye.yonei.entity.TransactionEntry;
import com.mgaye.yonei.service.TransactionEntryService;

import jakarta.persistence.EntityNotFoundException;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/entries")
public class TransactionEntryController {
    private final TransactionEntryService entryService;

    public TransactionEntryController(TransactionEntryService entryService) {
        this.entryService = entryService;
    }

    @PostMapping
    public ResponseEntity<TransactionEntryDto> createEntry(
            @RequestBody TransactionEntryDto request) {
        try {
            TransactionEntry entry = entryService.createEntry(
                    request.getUserId(),
                    request.getTransferId(),
                    request.getAmount());
            return ResponseEntity.ok(TransactionEntryDto.fromEntity(entry));
        } catch (EntityNotFoundException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<TransactionEntryDto> getEntry(@PathVariable Long id) {
        return entryService.findById(id)
                .map(TransactionEntryDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/user/{userId}/head")
    public ResponseEntity<TransactionEntryDto> getUserHead(@PathVariable Long userId) {
        return entryService.findUserHead(userId)
                .map(TransactionEntryDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @GetMapping("/user/{userId}/tail")
    public ResponseEntity<TransactionEntryDto> getUserTail(@PathVariable Long userId) {
        return entryService.findUserTail(userId)
                .map(TransactionEntryDto::fromEntity)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    // When you are not using linked-list
    @GetMapping("/user/{userId}/history")
    public List<TransactionEntryDto> getUserHistory(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "20") int limit) {
        return entryService.findByUserIdOrderByCreatedAtDesc(userId, limit)
                .stream()
                .map(TransactionEntryDto::fromEntity)
                .toList();
    }

}
