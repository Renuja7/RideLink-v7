package com.ridelink.ride.security;

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
 * are public, which require authentication, which require a specific role,
 * and how the JWT filter plugs into the security filter chain. This is what
 * implements the assignment's role-based authorisation requirement for
 * passenger vs driver operations.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Defines the HTTP security rules applied to every request handled by
     * this service.
     *
     * @param http Spring Security's fluent configuration builder.
     * @return the assembled filter chain Spring installs for this service.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is for browser/session-based apps; this is a
                // stateless JSON API secured by JWTs, so CSRF tokens don't apply here.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        // Swagger UI/OpenAPI docs must stay reachable without a token,
                        // since they are the assignment's official way to explore/demo the API.
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                        // Health/metrics endpoints are not sensitive business data.
                        .requestMatchers("/actuator/**").permitAll()
                        // Business rule: only a logged-in PASSENGER may create a ride request.
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/rides").hasRole("PASSENGER")
                        // Business rule: only a logged-in DRIVER may accept, start or
                        // complete a ride (the driver-facing lifecycle actions).
                        .requestMatchers(org.springframework.http.HttpMethod.PUT, "/api/rides/*/accept",
                                "/api/rides/*/start", "/api/rides/*/complete").hasRole("DRIVER")
                        // Everything else just needs to be authenticated (any valid role).
                        .anyRequest().authenticated()
                )
                // No HTTP session is created/used — every request must carry its own
                // JWT, which fits a horizontally-scalable microservice.
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Runs our JWT check before Spring's default username/password filter,
                // so the SecurityContext is populated from the token early in the chain.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
