package com.mgaye.yonei.dto.response;

import com.mgaye.yonei.dto.UserDto;

import lombok.Data;

@Data
public class LoginResponse {
    private String message;
    private UserDto user;
    private boolean success;

    public static LoginResponse success(UserDto user) {
        LoginResponse response = new LoginResponse();
        response.setMessage("Login successful");
        response.setUser(user);
        response.setSuccess(true);
        return response;
    }

    public static LoginResponse error(String message) {
        LoginResponse response = new LoginResponse();
        response.setMessage(message);
        response.setSuccess(false);
        return response;
    }
}