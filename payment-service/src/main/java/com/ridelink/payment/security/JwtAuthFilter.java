package com.ridelink.payment.security;

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
 * A Spring Security filter that runs once per request, reads the JWT from
 * the Authorization header (issued originally by the Account Service) and,
 * if it is valid and not expired, populates the SecurityContext so the rest
 * of the request pipeline knows who the caller is and what role they have.
 * This lets this service verify tokens locally without calling back to the
 * Account Service on every request.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Inspects the incoming request's Authorization header. If it carries a
     * "Bearer <token>" JWT, this decodes it, checks it hasn't expired, and
     * (when nobody is authenticated yet for this request) sets up a Spring
     * Security authentication token with the user's email as principal and
     * their role as a granted authority. Any missing/invalid/expired token
     * is silently ignored here - Spring Security's own access rules then
     * reject the request later if authentication turns out to be required.
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        // No bearer token at all - just continue the filter chain unauthenticated.
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
                    // Spring Security expects authorities prefixed with "ROLE_".
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Any parsing/signature error means an untrusted or malformed token -
            // log it and fall through unauthenticated rather than failing the request here.
            logger.warn("Invalid JWT token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
