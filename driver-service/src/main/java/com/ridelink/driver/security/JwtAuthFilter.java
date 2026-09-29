package com.ridelink.driver.security;

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
 * Servlet filter that runs once per request to authenticate callers via a
 * Bearer JWT, without ever calling Account Service over the network.
 * It independently verifies the token's signature and expiry (using the
 * shared JWT_SECRET) and, if valid, populates Spring Security's context with
 * the caller's identity and role so downstream authorisation rules (see
 * SecurityConfig) can be enforced.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No/foreign auth header: let the request through unauthenticated -
        // SecurityConfig will still block it later if the endpoint requires auth.
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        final String token = authHeader.substring(7); // strip the "Bearer " prefix

        try {
            final String email = jwtService.extractEmail(token);

            // Only authenticate if we haven't already done so for this request.
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!jwtService.isTokenExpired(token)) {
                    String role = jwtService.extractRole(token);
                    // Spring Security expects authority names prefixed with "ROLE_".
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken); // marks the request as authenticated
                }
                // else: expired token, treated as unauthenticated (no exception thrown here)
            }
        } catch (Exception e) {
            // Malformed/invalid-signature tokens are logged and swallowed rather than
            // failing the request outright - the request simply proceeds unauthenticated.
            logger.warn("Invalid JWT token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
