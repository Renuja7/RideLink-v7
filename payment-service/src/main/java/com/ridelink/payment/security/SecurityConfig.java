package com.ridelink.payment.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration for this service: which endpoints
 * are public, which require authentication, and how the JWT filter plugs
 * into the standard security filter chain. @EnableMethodSecurity allows
 * method-level role checks (e.g. @PreAuthorize) elsewhere if needed.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Defines the HTTP security rules: Swagger/OpenAPI docs and actuator
     * health endpoints are open to everyone (so Swagger UI itself is
     * reachable without a token), every other endpoint requires a valid
     * authenticated caller, sessions are stateless (JWT-based, no server-side
     * session state - fits a horizontally scalable microservice), and the
     * custom JwtAuthFilter runs before Spring's own username/password filter
     * so the JWT is what actually establishes who the caller is.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // not needed for a stateless, token-based JSON API
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        .requestMatchers("/actuator/**").permitAll()
                        // Fare estimation and payment creation are also invoked server-to-server
                        // by ride-service via OpenFeign, so they remain authenticated but role-open.
                        .anyRequest().authenticated()
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
