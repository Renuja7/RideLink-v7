package com.ridelink.payment.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.function.Function;

/**
 * Low-level helper for decoding and validating JWTs issued by the Account
 * Service. This service does not issue tokens itself - it only verifies
 * them, using a shared secret (configured via the jwt.secret property, kept
 * out of source control per the assignment's secrets-handling rule) so both
 * services agree on the same signing key.
 */
@Service
public class JwtService {

    @Value("${jwt.secret}")
    private String secret;

    /** Builds the HMAC signing key from the shared secret used to verify token signatures. */
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(secret.getBytes());
    }

    /** Reads the token's subject claim, which this system uses to carry the user's email. */
    public String extractEmail(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /** Reads the custom "role" claim (e.g. PASSENGER, DRIVER, ADMIN) used for authorization. */
    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    /** Returns true if the token's expiration time has already passed. */
    public boolean isTokenExpired(String token) {
        return extractClaim(token, Claims::getExpiration).before(new Date());
    }

    /**
     * Generic helper that decodes all claims from the token and applies the
     * given resolver function to pull out a specific one (e.g. subject,
     * expiration). Keeps the individual extractXxx methods above concise.
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Parses and cryptographically verifies the JWT's signature using the
     * shared secret, then returns its claims payload. Throws if the
     * signature is invalid or the token is malformed.
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
