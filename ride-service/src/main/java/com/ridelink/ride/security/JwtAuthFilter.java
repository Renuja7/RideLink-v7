package com.ridelink.ride.security;

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
 * Servlet filter that runs once per incoming HTTP request (before it reaches
 * any controller) and, if a valid "Bearer <jwt>" Authorization header is
 * present, populates Spring Security's SecurityContext with the caller's
 * identity and role. This is what lets @PreAuthorize / requestMatchers rules
 * in SecurityConfig know who is calling and what role they have. Note this
 * service only verifies tokens issued by account-service (via the same
 * shared jwt.secret) — it does not issue tokens itself.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    /**
     * Inspects the request for a Bearer token and, if it is present and
     * valid, authenticates the current request in Spring Security's context
     * so downstream authorization checks succeed. Always calls
     * filterChain.doFilter at the end (or immediately, if there's no token
     * to process) so the request continues normally either way — an
     * invalid/missing token simply leaves the request unauthenticated rather
     * than blocking it here (SecurityConfig decides whether that endpoint
     * requires authentication).
     */
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                     @NonNull HttpServletResponse response,
                                     @NonNull FilterChain filterChain) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            // No bearer token supplied (e.g. a public endpoint like Swagger UI,
            // or an internal Feign call missing the header) — just move on and
            // let SecurityConfig's rules decide if this request is allowed anonymously.
            filterChain.doFilter(request, response);
            return;
        }

        // Strip the "Bearer " prefix (7 characters) to get the raw JWT string.
        final String token = authHeader.substring(7);

        try {
            final String email = jwtService.extractEmail(token);

            // Only authenticate if we got an email AND nothing has already
            // authenticated this request (avoids overwriting an existing
            // security context on internal forwards/re-invocations).
            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (!jwtService.isTokenExpired(token)) {
                    String role = jwtService.extractRole(token);
                    // Spring Security expects role authorities prefixed with "ROLE_"
                    // (so hasRole("PASSENGER") matches an authority of "ROLE_PASSENGER").
                    var authorities = List.of(new SimpleGrantedAuthority("ROLE_" + role));

                    UsernamePasswordAuthenticationToken authToken =
                            new UsernamePasswordAuthenticationToken(email, null, authorities);
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    // Registers this identity for the rest of the request's processing,
                    // so requestMatchers(...).hasRole(...) checks in SecurityConfig work.
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
                // If the token is expired, we simply skip authenticating — the
                // request proceeds as anonymous, and protected endpoints will
                // then reject it via SecurityConfig's rules.
            }
        } catch (Exception e) {
            // Malformed/tampered token, wrong signature, etc. We deliberately
            // don't fail the request here — we just log a warning and leave it
            // unauthenticated, letting SecurityConfig's access rules do the rejecting.
            logger.warn("Invalid JWT token: " + e.getMessage());
        }

        filterChain.doFilter(request, response);
    }
}
