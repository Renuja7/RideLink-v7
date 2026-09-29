package com.ridelink.account.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * Servlet filter that runs once per incoming request and turns a valid "Authorization:
 * Bearer <jwt>" header into a Spring Security authentication in the SecurityContext.
 * It validates the token's signature and expiry entirely on its own (using the shared
 * JWT_SECRET) — it never calls another service or the database to do this, which is
 * exactly what lets every RideLink microservice trust a token issued by Account Service.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Inspects each request for a bearer token; if present and valid, populates the
     * SecurityContext with an authenticated principal (the user's email) and a
     * "ROLE_xxx" authority derived from the token's role claim, so downstream
     * @PreAuthorize checks and SecurityConfig's authorizeHttpRequests rules can work.
     * Requests with no token, or an invalid/expired one, simply continue unauthenticated
     * — they are then rejected later by SecurityConfig if the endpoint requires auth.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No token at all, or not a bearer token: let the request through unauthenticated
        // (public endpoints like /api/auth/** don't need one; protected ones will be
        // rejected further down the filter chain by SecurityConfig).
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7); // Strip the "Bearer " prefix to get the raw JWT

        try {
            final String email = jwtService.extractEmail(token); // Subject claim = the user's email

            // Only set up authentication if we haven't already done so for this request,
            // and only if the token hasn't expired.
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!jwtService.isTokenExpired(token)) {
                    String role = jwtService.extractRole(token);
                    // Spring Security expects authorities prefixed with "ROLE_" for hasRole() checks to work.
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Register the authenticated user for the rest of this request's processing.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Malformed/tampered/expired tokens land here; we deliberately swallow the
            // exception and just log a warning instead of failing the request outright —
            // the request simply proceeds unauthenticated and gets rejected downstream
            // if the endpoint requires a role.
            logger.warn("Invalid JWT token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
