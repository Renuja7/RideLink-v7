package com.ridelink.ride.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Reads and validates JWTs issued by account-service. This service does not
 * issue tokens itself (only account-service's Account Service does that on
 * login) — JwtService here only verifies the signature (using a secret
 * shared across services via the jwt.secret configuration property/
 * environment variable, never committed to source control) and extracts
 * claims like email, role and user id so JwtAuthFilter can authenticate
 * the request.
 */
@Service
public class JwtService {

    // Shared HMAC signing secret, injected from configuration (environment
    // variable/secrets manager) — never hard-coded, so it isn't committed to git.
    @Value("${jwt.secret}")
    private String secret;

    // Builds the cryptographic key used to verify a JWT's signature from the
    // shared secret string.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * @param token a signed JWT.
     * @return the "subject" claim, which account-service sets to the user's
     *         email — used as the authenticated principal's identity.
     */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * @param token a signed JWT.
     * @return the custom "role" claim (e.g. PASSENGER, DRIVER, ADMIN), used
     *         to build the ROLE_ authority for Spring Security.
     */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * @param token a signed JWT.
     * @return the custom "userId" claim, if this service ever needs the raw
     *         numeric user id rather than the email.
     */
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /**
     * @param token a signed JWT.
     * @return true if the token's expiration time has already passed, so
     *         JwtAuthFilter knows to treat it as unauthenticated rather than trust it.
     */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Generic helper to pull any single claim out of a token using a
     * caller-supplied extractor function (e.g. Claims::getSubject), so each
     * specific extractXxx method above doesn't need to repeat the parsing logic.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parses and verifies the token's signature against our signing key, and
    // returns all of its claims. Throws if the signature is invalid, the
    // token is malformed, or it has otherwise been tampered with.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
