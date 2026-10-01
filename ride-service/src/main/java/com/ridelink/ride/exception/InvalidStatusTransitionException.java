package com.ridelink.ride.exception;

/**
 * Thrown by RideService when a caller tries to move a ride to a status that
 * is not reachable from its current status (per the VALID_TRANSITIONS
 * lifecycle map), e.g. trying to complete a ride that hasn't started yet.
 * Mapped to HTTP 400 Bad Request by GlobalExceptionHandler; demonstrating
 * this is one of the assignment's required negative scenarios.
 */
public class InvalidStatusTransitionException extends RuntimeException {
    /**
     * @param message human-readable explanation of the illegal transition
     *                (e.g. "Cannot transition ride from REQUESTED to COMPLETED").
     */
    public InvalidStatusTransitionException(String message) {
        super(message);
    }
}
