package com.ridelink.driver.exception;

/**
 * Thrown when a requested driver or vehicle cannot be found by id.
 * Translated by {@link GlobalExceptionHandler} into an HTTP 404 Not Found
 * response.
 */
public class ResourceNotFoundException extends RuntimeException {
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
