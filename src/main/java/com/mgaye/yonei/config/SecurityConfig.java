package com.mgaye.yonei.config;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Map;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import com.mgaye.yonei.security.CustomUserDetailsService;
import com.mgaye.yonei.security.JwtFilter;
import com.mgaye.yonei.security.JwtUtil;

@Configuration
public class SecurityConfig {

        private final JwtUtil jwtUtil;
        private final CustomUserDetailsService userDetailsService;

        public SecurityConfig(JwtUtil jwtUtil,
                        CustomUserDetailsService userDetailsService) {
                this.jwtUtil = jwtUtil;
                this.userDetailsService = userDetailsService;
        }

        @Bean
        public JwtFilter jwtFilter() {
                return new JwtFilter(jwtUtil, userDetailsService);
        }

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                http
                                .csrf(csrf -> csrf
                                                .ignoringRequestMatchers(
                                                                "/api/users/register",
                                                                "/api/auth/login",
                                                                "/api/users/verify-email",
                                                                "/api/users/verify-email-page/**",
                                                                "/api/users/test-simple",
                                                                "/api/users/debug/**",
                                                                "/api/beneficiaries/**")) // 🔥 ADD THIS LINE
                                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                                // Add security headers
                                .headers(headers -> headers
                                                .contentSecurityPolicy(csp -> csp
                                                                .policyDirectives(
                                                                                "default-src 'self'; script-src 'self'; style-src 'self'"))
                                                .httpStrictTransportSecurity(hsts -> hsts
                                                                .includeSubDomains(true)
                                                                .maxAgeInSeconds(31536000)))
                                .authorizeHttpRequests(auth -> auth
                                                .requestMatchers(
                                                                "/api/users/register",
                                                                "/api/auth/login",
                                                                "/api/users/verify-email",
                                                                "/api/users/verify-email-page/**",
                                                                "/api/users/simple-test",
                                                                "/api/users/debug/**",
                                                                "/test-simple",
                                                                "/template-test",
                                                                "/api/debug/**") // 🔥 Add debug endpoints to permitted

                                                .permitAll()
                                                .requestMatchers("/css/**", "/js/**", "/ts/**", "/images/**")
                                                .permitAll()
                                                .requestMatchers("/api/beneficiaries/**").authenticated() // 🔥
                                                                                                          // EXPLICITLY
                                                                                                          // ALLOW
                                                .anyRequest().authenticated())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
                return http.build();
        }

        @Bean
        public CorsConfigurationSource corsConfigurationSource() {
                CorsConfiguration configuration = new CorsConfiguration();

                // Expo / web development origins
                configuration.setAllowedOriginPatterns(Arrays.asList(
                                "http://localhost:19006", // Expo web
                                "http://192.168.1.100:19006", // web on LAN
                                "exp://192.168.1.100:8081", // Expo Go / device
                                "exp://*" // Expo development

                ));

                // // Allow all localhost and local network origins for development
                // configuration.setAllowedOriginPatterns(Arrays.asList(
                // "http://localhost:*", // All localhost ports
                // "http://127.0.0.1:*", // All localhost ports
                // "http://192.168.*.*:*", // All local network IPs
                // "http://10.0.2.2:*", // Android emulator
                // "exp://*" // Expo development
                // ));

                // HTTP methods your frontend will call
                configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

                // Allow all headers
                configuration.setAllowedHeaders(Arrays.asList("*"));

                // Allow cookies/credentials
                configuration.setAllowCredentials(true);

                configuration.setMaxAge(3600L); // 1 hour

                UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
                source.registerCorsConfiguration("/**", configuration);
                return source;
        }

}
