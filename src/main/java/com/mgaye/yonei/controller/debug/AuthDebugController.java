package com.mgaye.yonei.controller.debug;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/debug")
public class AuthDebugController {

    @GetMapping("/auth-status")
    public Map<String, Object> authStatus(Principal principal, HttpServletRequest request) {
        Map<String, Object> result = new HashMap<>();

        // Check Principal
        result.put("principal", principal != null ? principal.getName() : "null");

        // Check Spring Security Context
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        result.put("authentication", auth != null ? auth.getName() : "null");
        result.put("authenticated", auth != null && auth.isAuthenticated());
        result.put("authClass", auth != null ? auth.getClass().getSimpleName() : "null");
        result.put("authorities", auth != null ? auth.getAuthorities().toString() : "null");

        // Check cookies
        if (request.getCookies() != null) {
            Map<String, String> cookies = new HashMap<>();
            for (Cookie cookie : request.getCookies()) {
                cookies.put(cookie.getName(), cookie.getValue());
            }
            result.put("cookies", cookies);
        }

        return result;
    }

    @GetMapping("/test-protected")
    @PreAuthorize("isAuthenticated()")
    public String testProtected() {
        return "✅ Protected endpoint accessed successfully!";
    }
}
