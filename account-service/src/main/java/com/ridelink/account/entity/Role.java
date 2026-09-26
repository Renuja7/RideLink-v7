package com.ridelink.account.entity;

/**
 * The set of roles a RideLink account can have.
 * This value is embedded in issued JWTs as a claim, and Spring Security turns it into
 * a "ROLE_xxx" authority (see JwtAuthFilter) so that @PreAuthorize("hasRole('ADMIN')")
 * checks work consistently across endpoints and, potentially, other services.
 */
public enum Role {
    PASSENGER,
    DRIVER,
    ADMIN
}
