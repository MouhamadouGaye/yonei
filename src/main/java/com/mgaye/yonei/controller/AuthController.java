
package com.mgaye.yonei.controller;

import com.mgaye.yonei.dto.UserDto;
import com.mgaye.yonei.dto.request.LoginRequest;
import com.mgaye.yonei.dto.response.LoginResponse;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.security.JwtUtil;
import com.mgaye.yonei.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import java.util.Map;

import org.apache.tomcat.websocket.AuthenticationException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:4200", allowCredentials = "true")
public class AuthController {

    private final UserService userService;
    private final JwtUtil jwtUtil;

    public AuthController(UserService userService, JwtUtil jwtUtil) {
        this.userService = userService;
        this.jwtUtil = jwtUtil;
    }

    // // Change to return JSON instead of plain text
    // @PostMapping("/login")
    // public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
    // HttpServletResponse response) {
    // try {
    // if (!userService.checkCredentials(request.getEmail(), request.getPassword()))
    // {
    // throw new RuntimeException("Invalid credentials");
    // }

    // User user = userService.getByEmail(request.getEmail());

    // if (!user.isEmailVerified()) {
    // return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
    // .body(LoginResponse.error("Please verify your email address before logging
    // in."));
    // }

    // String token = jwtUtil.generateToken(user.getEmail());

    // Cookie cookie = new Cookie("jwt", token);
    // cookie.setHttpOnly(true);
    // cookie.setPath("/");
    // cookie.setSecure(false); // Only over HTTPS
    // cookie.setAttribute("SameSite", "Strict");
    // cookie.setMaxAge(15 * 60); // 24h
    // response.addCookie(cookie);

    // UserDto userDto = UserDto.fromEntity(user, false);
    // userDto.setAuthenticated(true);
    // userDto.setEmailVerified(true);

    // return ResponseEntity.ok().body(LoginResponse.success(userDto));

    // } catch (RuntimeException e) {
    // return ResponseEntity.status(401).body(LoginResponse.error("Invalid email or
    // password "));
    // }
    // }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request, HttpServletResponse response) {
        try {
            if (!userService.checkCredentials(request.getEmail(), request.getPassword())) {
                throw new RuntimeException("Invalid credentials");
            }

            User user = userService.getByEmail(request.getEmail());

            if (!user.isEmailVerified()) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body(LoginResponse.error("Please verify your email address before logging in."));
            }

            String token = jwtUtil.generateToken(user.getEmail());

            // Create JWT cookie
            ResponseCookie jwtCookie = ResponseCookie.from("jwt", token)
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .maxAge(15 * 60)
                    .sameSite("Lax") // Changed from Strict to Lax
                    .build();

            response.addHeader(HttpHeaders.SET_COOKIE, jwtCookie.toString());

            // 🔥 Use the special method for login response
            UserDto userDto = UserDto.forLoginResponse(user, false);
            userDto.setAuthenticated(true); // Explicitly set to true after login

            return ResponseEntity.ok().body(LoginResponse.success(userDto));

        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(LoginResponse.error("Invalid email or password"));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletResponse response) {
        Cookie cookie = new Cookie("jwt", null);
        cookie.setHttpOnly(true);
        cookie.setPath("/");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
        return ResponseEntity.ok().body(Map.of("Error", "Logged out successfully"));
    }

}
