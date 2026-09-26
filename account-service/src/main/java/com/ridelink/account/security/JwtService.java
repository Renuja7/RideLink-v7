package com.ridelink.account.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Creates and validates the JSON Web Tokens used across all of RideLink for
 * authentication. Tokens are signed with HMAC-SHA256 (via the jjwt 0.12.x library)
 * using a secret shared by every microservice through the JWT_SECRET environment
 * variable — this is the mechanism that lets Driver & Vehicle, Ride Management and
 * Fare & Payment services verify a caller's identity/role without ever calling back
 * into Account Service.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret; // Shared signing secret, injected from the JWT_SECRET env var — never hardcoded

    @Value("${jwt.expiration-ms}")
    private long expirationMs; // How long a token stays valid, in milliseconds

    // Builds the HMAC-SHA256 signing key from the configured secret. Kept private since
    // no other class needs the raw key, only the higher-level sign/verify operations below.
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /**
     * Issues a new signed JWT for a successfully authenticated user.
     * The subject is set to the user's email, with custom "userId" and "role" claims
     * added so that any service holding the token can identify the caller and check
     * permissions without a database lookup. The token is stamped with an issued-at
     * time and an expiry (now + expirationMs), then signed so it cannot be tampered with.
     */
    public String generateToken(String email, String userId, String role) {
        return Jwts.builder()
                .subject(email)
                .claim("userId", userId)
                .claim("role", role)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationMs))
                .signWith(getSigningKey()) // Signs the token so any tampering invalidates it
                .compact();
    }

    /** Reads the subject claim (the user's email) back out of a token. */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Reads the custom "userId" claim back out of a token. */
    public String extractUserId(String token) {
        return extractAllClaims(token).get("userId", String.class);
    }

    /** Reads the custom "role" claim back out of a token (e.g. "PASSENGER", "DRIVER", "ADMIN"). */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /**
     * Checks that a token belongs to the given email and hasn't expired. Useful when
     * a caller already knows who they expect the token to represent.
     */
    public boolean isTokenValid(String token, String email) {
        String extractedEmail = extractEmail(token);
        return extractedEmail.equals(email) && !isTokenExpired(token);
    }

    /** True if the token's expiration timestamp is in the past. */
    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    /** Reads the expiration timestamp out of a token. */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Generic helper: parses/validates the token, then applies the given function to
     * pull out whichever claim the caller needs.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    // Parses the JWT and verifies its signature against the shared secret in one step —
    // if the signature doesn't match (wrong secret, tampered payload) or the token is
    // malformed, this throws, which callers (like JwtAuthFilter) catch and treat as
    // "not authenticated".
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
