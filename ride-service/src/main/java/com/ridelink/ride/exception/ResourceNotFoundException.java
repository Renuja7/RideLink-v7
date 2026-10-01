package com.ridelink.ride.exception;

/**
 * Thrown when a requested resource (currently, a Ride looked up by id) does
 * not exist in this service's database. Mapped to HTTP 404 Not Found by
 * GlobalExceptionHandler.
 */
public class ResourceNotFoundException extends RuntimeException {
    /**
     * @param message human-readable explanation of what was not found.
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
