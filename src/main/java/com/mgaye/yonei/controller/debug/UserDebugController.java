package com.mgaye.yonei.controller.debug;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.UserRepository;

@RestController
@RequestMapping("/api/debug")
public class UserDebugController {

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/user-lookup")
    public Map<String, Object> userLookup(@RequestParam String email, Principal principal) {
        Map<String, Object> result = new HashMap<>();

        result.put("principalEmail", principal != null ? principal.getName() : "null");
        result.put("lookupEmail", email);

        // Test user lookup
        Optional<User> user = userRepository.findByEmail(email);
        result.put("userFound", user.isPresent());
        result.put("userEmail", user.map(User::getEmail).orElse("NOT_FOUND"));
        result.put("userId", user.map(User::getId).orElse(-1L));

        // Count users with this email
        long count = userRepository.countByEmail(email);
        result.put("userCount", count);

        return result;
    }
}