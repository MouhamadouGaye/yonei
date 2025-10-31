package com.mgaye.yonei.dto.request;

import com.mgaye.yonei.entity.Beneficiary;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class BeneficiaryDTO {
    private Long id;
    private String fullName;
    private String phoneNumber;
    private String email;
    private String countryCode;
    private String countryName; // Added for frontend display
    private String currency; // Added for frontend display
    private String currencySymbol; // Very useful for frontend!

    public static BeneficiaryDTO from(Beneficiary entity) {
        return BeneficiaryDTO.builder()
                .id(entity.getId())
                .fullName(entity.getFullName())
                .phoneNumber(entity.getPhoneNumber())
                .email(entity.getEmail())
                .countryCode(entity.getCountryCode())
                .countryName(entity.getCountryName())
                .currency(entity.getCurrency())
                .currencySymbol(entity.getCurrencySymbol())
                .build();
    }
}
