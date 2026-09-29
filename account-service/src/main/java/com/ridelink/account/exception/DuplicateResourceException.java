package com.ridelink.account.exception;

/**
 * Thrown when an operation would create a resource that already exists — in this
 * service, specifically when registering with an email address that is already in use.
 * Mapped by GlobalExceptionHandler to HTTP 409 Conflict.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
