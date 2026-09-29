package com.ridelink.driver.exception;

/**
 * Thrown when an operation would create a resource that already exists, e.g.
 * a driver profile for a userId that already has one, or a vehicle number
 * that is already registered. Translated by {@link GlobalExceptionHandler}
 * into an HTTP 409 Conflict response.
 */
public class DuplicateResourceException extends RuntimeException {
    public DuplicateResourceException(String message) {
        super(message);
    }
}
