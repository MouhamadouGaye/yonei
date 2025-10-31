package com.mgaye.yonei.dto;

import com.mgaye.yonei.entity.User;
import lombok.Data;

@Data
public class UserDto {
    private Long id;
    private String username;
    private String email;
    private String password;
    private String phoneNumber;
    private boolean hasSavedCard; // 🔹 new field
    private boolean emailVerified;
    private boolean authenticated; // Add this
    private boolean requiresVerification; // Add this
    private String message; // Add this

    // ✅ Convert from entity
    public static UserDto fromEntity(User user, boolean hasSavedCard) {
        UserDto dto = new UserDto();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setEmail(user.getEmail());
        dto.setPhoneNumber(user.getPhoneNumber());
        dto.setHasSavedCard(hasSavedCard);
        dto.setEmailVerified(user.isEmailVerified());
        dto.setAuthenticated(false); // Default to false for registration
        dto.setRequiresVerification(!user.isEmailVerified());
        return dto;
    }

}