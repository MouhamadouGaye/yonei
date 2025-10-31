package com.mgaye.yonei.service;

import java.time.Instant;
import java.util.List;

import org.springframework.stereotype.Service;

import com.mgaye.yonei.dto.CountryCodeEntry;
import com.mgaye.yonei.entity.Beneficiary;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.BeneficiaryRepository;

import jakarta.transaction.Transactional;

@Service
@Transactional
public class BeneficiaryService {

    private final BeneficiaryRepository beneficiaryRepository;
    private final CountryCodeService countryCodeService;
    private final AuditService auditService;

    public BeneficiaryService(BeneficiaryRepository beneficiaryRepository,
            CountryCodeService countryCodeService,
            AuditService auditService) {
        this.beneficiaryRepository = beneficiaryRepository;
        this.countryCodeService = countryCodeService;
        this.auditService = auditService;
    }

    // ✅ IMPLEMENT THIS METHOD to fill all fields
    public Beneficiary createBeneficiary(User owner, String fullName, String phoneNumber, String email) {

        // Get comprehensive country info from phone number
        CountryCodeEntry country = countryCodeService.getCountryByPhoneNumber(phoneNumber);

        // Set default values if country not found
        String countryCode = country != null ? country.getCode() : "INTL";
        String countryName = country != null ? country.getName() : "International";
        String currency = country != null ? country.getCurrency() : "USD";
        String currencySymbol = country != null ? country.getCurrencySymbol() : "$";

        // Check for duplicate beneficiaries for this user
        boolean exists = beneficiaryRepository.existsByPhoneNumberAndOwner(phoneNumber, owner);
        if (exists) {
            throw new RuntimeException("This beneficiary already exists");
        }

        // Create beneficiary with ALL fields populated
        Beneficiary beneficiary = Beneficiary.builder()
                .owner(owner)
                .fullName(fullName)
                .phoneNumber(phoneNumber)
                .email(email)
                .countryCode(countryCode)
                .countryName(countryName)
                .currency(currency)
                .currencySymbol(currencySymbol)
                .createdAt(Instant.now())
                .build();

        Beneficiary savedBeneficiary = beneficiaryRepository.save(beneficiary);

        // Log the creation
        auditService.logSecurityEvent(owner.getId(), "BENEFICIARY_CREATED",
                String.format("Added beneficiary %s from %s (%s)", fullName, countryName, currency));

        return savedBeneficiary;
    }

    // Other methods...
    public List<Beneficiary> getUserBeneficiaries(Long userId) {
        return beneficiaryRepository.findByOwnerId(userId);
    }

    public Beneficiary getBeneficiaryById(Long beneficiaryId, Long userId) {
        return beneficiaryRepository.findByIdAndOwnerId(beneficiaryId, userId)
                .orElseThrow(() -> new RuntimeException("Beneficiary not found"));
    }
}