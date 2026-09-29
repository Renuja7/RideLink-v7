package com.ridelink.account.exception;

/**
 * Thrown when a requested resource (in this service, typically a user by id) cannot
 * be found. Mapped by GlobalExceptionHandler to HTTP 404 Not Found.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
