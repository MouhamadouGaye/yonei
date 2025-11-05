package com.mgaye.yonei.dto;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.service.CountryCodeService;

import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String Name;
    private String email;
    private String password;
    private String phoneNumber;
    private boolean hasSavedCard;
    private boolean emailVerified;
    private boolean authenticated; // This should reflect actual auth status
    private boolean requiresVerification;
    private String message;
    private String currency;

    // ✅ FIXED: Check actual authentication status
    public static UserDto fromEntity(User user, boolean hasSavedCard) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setHasSavedCard(hasSavedCard);
        dto.setEmailVerified(user.isEmailVerified());
        dto.setCurrency(user.getCurrency());

        // 🔥 CRITICAL: Check actual authentication status from SecurityContext
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        boolean isAuthenticated = auth != null &&
                auth.isAuthenticated() &&
                !(auth instanceof AnonymousAuthenticationToken) &&
                auth.getName().equals(user.getEmail());

        dto.setAuthenticated(isAuthenticated);
        dto.setRequiresVerification(!user.isEmailVerified());
        return dto;
    }

    // ✅ Alternative method for login response
    public static UserDto forLoginResponse(User user, boolean hasSavedCard) {
        UserDto dto = fromEntity(user, hasSavedCard);
        // After login, user should be authenticated
        dto.setAuthenticated(true);
        return dto;
    }

    // // ✅ Utility to detect currency from phone number
    // public static String getCurrencyFromPhoneNumber(String phoneNumber) {

    // CountryCodeService countryCodeService = new CountryCodeService();
    // String currency = countryCodeService.getCurrencyByPhoneNumber(phoneNumber);
    // return currency;

    // }
}