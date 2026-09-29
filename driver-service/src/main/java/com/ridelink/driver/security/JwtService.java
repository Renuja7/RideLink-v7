package com.ridelink.driver.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Validates and reads JWT tokens issued by Account Service.
 * Driver Service does NOT call Account Service to check a token - it
 * independently verifies the token's signature and expiry using the same
 * shared secret (JWT_SECRET, injected via configuration/environment
 * variable rather than committed to the repo). This keeps authentication
 * fast and avoids a synchronous dependency on Account Service being up for
 * every single request.
 */
@Service
public class JwtService {

    // Loaded from application config (backed by an environment variable), never hard-coded.
    @Value("${jwt.secret}")
    private String secret;

    /** Builds the HMAC signing key from the shared secret, used to verify token signatures. */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Extracts the token subject, which is used as the authenticated user's email. */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Extracts the user's role (e.g. DRIVER, ADMIN) embedded in the token by Account Service. */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /** Extracts the account's userId claim, if callers need to know who the token belongs to. */
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /** Returns true if the token's expiry timestamp is already in the past. */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /** Generic helper to pull any single claim out of the token's payload. */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parses and verifies the token's signature against the shared secret;
    // throws if the signature is invalid or the token is malformed.
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
