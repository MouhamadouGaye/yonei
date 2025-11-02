package com.mgaye.yonei.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.UserDetails;

@SpringBootTest
class UserDetailsServiceTest {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testLoadUserByUsername() {
        // This should work without throwing exceptions
        UserDetails userDetails = customUserDetailsService.loadUserByUsername("kobeameth@gmail.com");
        assertNotNull(userDetails);
        assertEquals("kobeameth@gmail.com", userDetails.getUsername());
    }
}