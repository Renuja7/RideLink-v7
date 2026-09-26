package com.ridelink.account.entity;

/**
 * Lifecycle states for a user account.
 * Only ACTIVE accounts are allowed to log in (see AuthService.login); INACTIVE and
 * SUSPENDED both block login but are kept distinct so an admin can tell the difference
 * between a self/admin-deactivated account and one suspended for misconduct.
 */
public enum AccountStatus {
    ACTIVE,
    INACTIVE,
    SUSPENDED
}
