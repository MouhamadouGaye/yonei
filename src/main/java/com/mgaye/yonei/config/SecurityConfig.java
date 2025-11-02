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

import com.mgaye.yonei.security.JwtFilter;
import com.mgaye.yonei.security.JwtUtil;

// // @Configuration
// // @EnableWebSecurity
// // public class SecurityConfig {
// // private final JwtFilter jwtFilter;

// // public SecurityConfig(JwtFilter jwtFilter) {
// // this.jwtFilter = jwtFilter;
// // }

// // @Bean
// // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
// // Exception {
// // http.csrf().disable()
// // .authorizeHttpRequests(auth -> auth
// // .requestMatchers("/api/users/register", "/api/auth/login").permitAll()
// // .anyRequest().authenticated())
// // .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);

// // http.addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);
// // return http.build();
// // }

// // @Bean
// // public PasswordEncoder passwordEncoder() {
// // return new BCryptPasswordEncoder();
// // }
// // }

// @Configuration
// public class SecurityConfig {

//     private final JwtUtil jwtUtil;
//     private final UserDetailsService userDetailsService;

//     public SecurityConfig(JwtUtil jwtUtil, UserDetailsService userDetailsService) {
//         this.jwtUtil = jwtUtil;
//         this.userDetailsService = userDetailsService;
//     }

//     @Bean
//     public JwtFilter jwtFilter() {
//         return new JwtFilter(jwtUtil, userDetailsService);
//     }

//     // @Bean
//     // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
//     // Exception {
//     // http.csrf(csrf -> csrf
//     // // Disable CSRF for public endpoints (or configure properly)
//     // .ignoringRequestMatchers("/api/users/verify-email", "/verify-email"))
//     // .cors(cors -> cors.configurationSource(corsConfigurationSource())) // <---
//     // apply CORS
//     // .authorizeHttpRequests(auth -> auth
//     // .requestMatchers("/api/users/register", "/api/auth/login",
//     // "/api/users/simple-test",
//     // "/api/users/verif-email")
//     // .permitAll()
//     // // Allow access to static resources (CSS, JS, images)
//     // .requestMatchers("/css/**", "/js/**", "/ts/**", "/images/**").permitAll()
//     // .anyRequest().authenticated())
//     // .sessionManagement()
//     // .sessionCreationPolicy(SessionCreationPolicy.STATELESS);

//     // http.addFilterBefore(jwtFilter(),
//     // UsernamePasswordAuthenticationFilter.class);
//     // return http.build();
//     // }

//     // @Bean
//     // public SecurityFilterChain securityFilterChain(HttpSecurity http) throws
//     // Exception {
//     // http
//     // .csrf(csrf -> csrf
//     // // Disable CSRF for public endpoints
//     // .ignoringRequestMatchers(
//     // "/api/users/register",
//     // "/api/auth/login",
//     // "/api/users/verify-email",
//     // "/api/users/verify-email-page",
//     // "/api/users/test-simple"))
//     // .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//     // .authorizeHttpRequests(auth -> auth
//     // .requestMatchers(
//     // "/api/users/register",
//     // "/api/auth/login",
//     // "/api/users/verify-email",
//     // "/api/users/verify-email-page",
//     // "/api/users/simple-test",
//     // "/test-simple",
//     // "/template-test")
//     // .permitAll()
//     // // Allow access to static resources
//     // .requestMatchers("/css/**", "/js/**", "/ts/**", "/images/**").permitAll()
//     // .anyRequest().authenticated())
//     // .sessionManagement(session -> session
//     // .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

//     // http.addFilterBefore(jwtFilter(),
//     // UsernamePasswordAuthenticationFilter.class);
//     // return http.build();
//     // }

//     @Bean
//     public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
//         http
//                 .csrf(csrf -> csrf
//                         .ignoringRequestMatchers(
//                                 "/api/users/register",
//                                 "/api/auth/login",
//                                 "/api/users/verify-email",
//                                 "/api/users/verify-email-page",
//                                 "/api/users/test-simple"))
//                 .cors(cors -> cors.configurationSource(corsConfigurationSource()))
//                 // Add security headers
//                 .headers(headers -> headers
//                         .contentSecurityPolicy(csp -> csp
//                                 .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'"))
//                         .httpStrictTransportSecurity(hsts -> hsts
//                                 .includeSubDomains(true)
//                                 .maxAgeInSeconds(31536000)))
//                 .authorizeHttpRequests(auth -> auth
//                         .requestMatchers(
//                                 "/api/users/register",
//                                 "/api/auth/login",
//                                 "/api/users/verify-email",
//                                 "/api/users/verify-email-page",
//                                 "/api/users/simple-test",
//                                 "/test-simple",
//                                 "/template-test")
//                         .permitAll()
//                         .requestMatchers("/css/**", "/js/**", "/ts/**", "/images/**").permitAll()
//                         .anyRequest().authenticated())
//                 .sessionManagement(session -> session
//                         .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

//         http.addFilterBefore(jwtFilter(), UsernamePasswordAuthenticationFilter.class);
//         return http.build();
//     }

//     // @Bean
//     // public CorsConfigurationSource corsConfigurationSource() {
//     // CorsConfiguration configuration = new CorsConfiguration();
//     // // Angular frontend origin
//     // configuration.setAllowedOriginPatterns(Arrays.asList("http://localhost:4200",
//     // "http://localhost:5000"));
//     // // HTTP methods your frontend will call
//     // configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE",
//     // "OPTIONS"));

//     // // Allow all headers
//     // configuration.setAllowedHeaders(Arrays.asList("*"));

//     // // VERY IMPORTANT: allow cookies/credentials
//     // configuration.setAllowCredentials(true);

//     // UrlBasedCorsConfigurationSource source = new
//     // UrlBasedCorsConfigurationSource();
//     // source.registerCorsConfiguration("/**", configuration);
//     // return source;
//     // }

//     @Bean
//     public CorsConfigurationSource corsConfigurationSource() {
//         CorsConfiguration configuration = new CorsConfiguration();

//         // Expo / web development origins
//         configuration.setAllowedOriginPatterns(Arrays.asList(
//                 "http://localhost:19006", // Expo web
//                 "http://192.168.1.100:19006", // web on LAN
//                 "exp://192.168.1.100:8081" // Expo Go / device

//         ));

//         // HTTP methods your frontend will call
//         configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));

//         // Allow all headers
//         configuration.setAllowedHeaders(Arrays.asList("*"));

//         // Allow cookies/credentials
//         configuration.setAllowCredentials(true);

//         UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
//         source.registerCorsConfiguration("/**", configuration);
//         return source;
//     }

// }

@Configuration
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final UserDetailsService userDetailsService;

    public SecurityConfig(JwtUtil jwtUtil,
            @Qualifier("customUserDetailsService") UserDetailsService userDetailsService) {
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
                                "/api/users/verify-email-page",
                                "/api/users/test-simple",
                                "/api/beneficiaries/**")) // 🔥 ADD THIS LINE
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                // Add security headers
                .headers(headers -> headers
                        .contentSecurityPolicy(csp -> csp
                                .policyDirectives("default-src 'self'; script-src 'self'; style-src 'self'"))
                        .httpStrictTransportSecurity(hsts -> hsts
                                .includeSubDomains(true)
                                .maxAgeInSeconds(31536000)))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/users/register",
                                "/api/auth/login",
                                "/api/users/verify-email",
                                "/api/users/verify-email-page",
                                "/api/users/simple-test",
                                "/test-simple",
                                "/template-test",
                                "/api/debug/**") // 🔥 Add debug endpoints to permitted

                        .permitAll()
                        .requestMatchers("/css/**", "/js/**", "/ts/**", "/images/**").permitAll()
                        .requestMatchers("/api/beneficiaries/**").authenticated() // 🔥 EXPLICITLY ALLOW
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
