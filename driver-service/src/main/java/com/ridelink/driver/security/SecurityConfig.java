package com.ridelink.driver.security;

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
 * Central Spring Security configuration: defines which endpoints require
 * authentication/roles, and wires in the JWT-based filter chain.
 * The service is stateless (no HTTP sessions) since authentication is
 * carried entirely in the Bearer token on every request, which suits a
 * horizontally-scalable microservice.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Declares the authorisation rules and plugs {@link JwtAuthFilter} into
     * the filter chain ahead of Spring's default username/password filter.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // stateless JSON API with no browser form submissions, so CSRF protection isn't needed
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // API docs stay open for demonstration
                        .requestMatchers("/actuator/**").permitAll() // health/monitoring endpoints
                        // Only "authenticated" (any logged-in role), not role-restricted: both a
                        // passenger's own flow and Ride Service acting on a passenger's behalf
                        // need to read the list of available drivers.
                        .requestMatchers("/api/drivers/available").authenticated()
                        // Everything else under drivers/vehicles is restricted to drivers
                        // managing their own operational data, or admins.
                        .requestMatchers("/api/drivers/**", "/api/vehicles/**").hasAnyRole("DRIVER", "ADMIN")
                        .anyRequest().authenticated() // safe default: deny anonymous access to anything not explicitly listed
                )
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // no server-side session state
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class); // run our JWT check before Spring's default filter

        return http.build();
    }
}
