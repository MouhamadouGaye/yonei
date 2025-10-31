package com.mgaye.yonei.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.mgaye.yonei.dto.CountryCodeEntry;
import com.mgaye.yonei.dto.request.BeneficiaryDTO;
import com.mgaye.yonei.dto.request.BeneficiaryRequest;
import com.mgaye.yonei.entity.Beneficiary;
import com.mgaye.yonei.entity.CountryCode;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.BeneficiaryRepository;
import com.mgaye.yonei.repository.CountryCodeRepository;
import com.mgaye.yonei.repository.UserRepository;
import com.mgaye.yonei.service.AuditService;
import com.mgaye.yonei.service.CountryCodeService;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import java.security.Principal;
import java.time.Instant;
import java.util.List;

@RestController
@RequestMapping("/api/beneficiaries")
@RequiredArgsConstructor
public class BeneficiaryController {

        private final BeneficiaryRepository beneficiaryRepository;
        private final CountryCodeService countryCodeService;
        private final UserRepository userRepository;
        private final AuditService auditService;

        // ✅ Create new beneficiary with ALL fields populated
        @PostMapping
        @PreAuthorize("isAuthenticated()")
        public BeneficiaryDTO createBeneficiary(@Valid @RequestBody BeneficiaryRequest request,
                        Principal principal) {

                // 1. Find the logged-in user
                User owner = userRepository.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                                "User not found"));

                // 2. Check for duplicate beneficiaries
                boolean exists = beneficiaryRepository.existsByPhoneNumberAndOwner(request.getPhoneNumber(), owner);
                if (exists) {
                        throw new ResponseStatusException(HttpStatus.CONFLICT, "This beneficiary already exists");
                }

                // 3. Get comprehensive country info from phone number
                CountryCodeEntry country = countryCodeService.getCountryByPhoneNumber(request.getPhoneNumber());

                // 4. Set default values if country not found
                String countryCode = country != null ? country.getCode() : "INTL";
                String countryName = country != null ? country.getName() : "International";
                String currency = country != null ? country.getCurrency() : "USD";
                String currencySymbol = country != null ? country.getCurrencySymbol() : "$";

                // 5. Create beneficiary with ALL fields populated
                Beneficiary beneficiary = Beneficiary.builder()
                                .owner(owner)
                                .fullName(request.getFullName())
                                .phoneNumber(request.getPhoneNumber())
                                .email(request.getEmail())
                                .countryCode(countryCode)
                                .countryName(countryName)
                                .currency(currency)
                                .currencySymbol(currencySymbol)
                                .createdAt(Instant.now())
                                .build();

                Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);

                // 6. Log the creation
                auditService.logSecurityEvent(owner.getId(), "BENEFICIARY_CREATED",
                                String.format("Added beneficiary %s from %s (%s)",
                                                request.getFullName(), countryName, currency));

                // 7. Return enhanced DTO
                return BeneficiaryDTO.from(savedBeneficiary);
        }

        // ✅ Get all beneficiaries for current user
        @GetMapping
        @PreAuthorize("isAuthenticated()")
        public List<BeneficiaryDTO> getMyBeneficiaries(Principal principal) {
                User owner = userRepository.findByEmail(principal.getName())
                                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                                                "User not found"));

                return beneficiaryRepository.findByOwner(owner).stream()
                                .map(BeneficiaryDTO::from)
                                .toList();
        }
}
