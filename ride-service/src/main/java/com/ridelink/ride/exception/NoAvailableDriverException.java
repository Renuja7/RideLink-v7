package com.ridelink.ride.exception;

/**
 * Thrown by RideService#assignDriver when driver-service reports that there
 * are currently no available drivers to assign to a ride. Mapped to HTTP 409
 * Conflict by GlobalExceptionHandler; demonstrating this is one of the
 * assignment's required negative scenarios ("no available driver").
 */
public class NoAvailableDriverException extends RuntimeException {
    /**
     * @param message human-readable explanation of why assignment failed.
     */
    public NoAvailableDriverException(String message) {
        super(message);
    }
}
