package com.mgaye.yonei.controller;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.mgaye.yonei.dto.CardDTO;
import com.mgaye.yonei.entity.User;
import com.mgaye.yonei.repository.CardRepository;
import com.mgaye.yonei.repository.UserRepository;
import com.stripe.service.issuing.AuthorizationService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/cards")
@RequiredArgsConstructor
public class CardController {

    private final CardRepository cardRepository;
    private final UserRepository userRepository;

    @GetMapping("/me")
    public List<CardDTO> getMyCards(Authentication authentication) {
        String email = authentication.getName(); // Spring Security stores the email/username here
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return cardRepository.findByUserId(user.getId())
                .stream()
                .map(CardDTO::fromEntity)
                .toList();
    }
}
