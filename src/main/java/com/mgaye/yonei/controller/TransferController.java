package com.mgaye.yonei.controller;

import com.mgaye.yonei.dto.response.TransferResponseDto;
import com.mgaye.yonei.dto.TransferDTO;
import com.mgaye.yonei.dto.request.TransferRequestDto;
import com.mgaye.yonei.entity.Transfer;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.UserRepository;
import com.mgaye.yonei.service.TransferService;

import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.Instant;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/transfers")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;
    private final UserRepository userRepository;

    @PostMapping
    @PreAuthorize("isAuthenticated()")
    public TransferDTO createTransfer(@RequestBody TransferRequestDto request, Principal principal) {

        // 1. Validate the amount
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Amount must be greater than zero");
        }

        // 2. Ensure that at least one recipient is specified (internal user OR external
        // beneficiary)
        if (request.getToUserId() == null && request.getBeneficiaryId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Either toUserId or beneficiaryId must be provided");
        }

        // 3. Identify the sender from the authenticated principal (never trust frontend
        // IDs) & swz
        User sender = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        try {
            // 4. Delegate to the service layer:
            // - Handles internal vs external transfers
            // - Applies "fromCard" logic (skip balance check if true)
            Transfer transfer = transferService.createTransferWithStripe(
                    sender.getId(),
                    request.getToUserId(),
                    request.getBeneficiaryId(),
                    request.getAmount(),
                    request.isFromCard() // <-- new flag to indicate card funding
            );
            System.out.println("|||||||||||||||DATA: " + sender.getId() +
                    request.getToUserId() +
                    request.getBeneficiaryId() +
                    request.getAmount() +
                    request.isFromCard());

            // 5. Convert entity to DTO for the API response
            return TransferDTO.from(transfer);

        } catch (RuntimeException e) {
            // 6. Propagate service-level validation/business errors as HTTP 400
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, e.getMessage());
        }
    }

    // ✅ Get all transfers for current user
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public List<TransferDTO> getMyTransfers(Principal principal) {
        User current = userRepository.findByEmail(principal.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User not found"));

        return transferService.getUserTransfers(current.getId()).stream()
                .map(TransferDTO::from)
                .toList();
    }

}
