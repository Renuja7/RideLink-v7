package com.ridelink.payment.exception;

/**
 * Thrown when a requested payment (or a receipt for a non-successful/absent
 * payment) cannot be found. Caught by GlobalExceptionHandler and turned into
 * a 404 Not Found response.
 */
public class ResourceNotFoundException extends RuntimeException {
    /** Creates the exception carrying a human-readable "not found" reason. */
    public ResourceNotFoundException(String message) {
        super(message);
    }
}
