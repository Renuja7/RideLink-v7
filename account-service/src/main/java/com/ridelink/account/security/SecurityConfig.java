package com.ridelink.account.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Central Spring Security configuration for this service.
 * Uses Spring Security 6's SecurityFilterChain style (no WebSecurityConfigurerAdapter),
 * configures stateless (session-less) authentication since JWTs carry all the
 * identity info needed on every request, and enables method-level security
 * (@EnableMethodSecurity) so controllers can use @PreAuthorize("hasRole('ADMIN')").
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;

    /**
     * Provides the BCrypt password encoder used to hash passwords on registration and
     * verify them on login. BCrypt automatically salts each hash, so two users with the
     * same password end up with different stored hashes.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * Defines the HTTP security rules: which endpoints are public, which require a
     * specific role, and how authentication is performed (via our custom JWT filter
     * instead of Spring's default form login/session mechanism).
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF protection is for browser/cookie-based sessions; irrelevant for a
                // stateless, token-based JSON API, so it's disabled.
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/api/auth/**").permitAll() // Registration/login must be reachable without a token
                        .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll() // Swagger docs stay public for demoing the API
                        .requestMatchers("/actuator/**").permitAll() // Health/monitoring endpoints stay public
                        .requestMatchers("/api/users/**").hasAnyRole("ADMIN", "PASSENGER", "DRIVER") // Any authenticated account may hit user endpoints; finer-grained checks are done per-method with @PreAuthorize
                        .anyRequest().authenticated() // Everything else needs at least a valid token
                )
                // No HttpSession is created or used; every request must carry its own JWT.
                .sessionManagement(sess -> sess.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                // Runs our JWT check before Spring's built-in username/password filter,
                // since we never use that form-login mechanism at all.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
