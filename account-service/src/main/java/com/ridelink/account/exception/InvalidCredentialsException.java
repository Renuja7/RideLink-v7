package com.ridelink.account.exception;

/**
 * Thrown when a login attempt fails — either the email is not registered, the
 * password does not match, or the account is not ACTIVE. Mapped by
 * GlobalExceptionHandler to HTTP 401 Unauthorized.
 */
public class InvalidCredentialsException extends RuntimeException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}
