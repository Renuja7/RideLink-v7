package com.ridelink.ride.entity;

/**
 * The states a Ride can be in, and implicitly the ride lifecycle workflow
 * required by the assignment. The normal "happy path" moves forward one
 * step at a time:
 *
 *   REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
 *
 * CANCELLED can be reached from any of REQUESTED, ASSIGNED, ACCEPTED or
 * IN_PROGRESS, but never from COMPLETED. Skipping a step (e.g. REQUESTED
 * straight to COMPLETED) is not allowed. The actual rules are enforced in
 * RideService's VALID_TRANSITIONS map, not here — this enum only defines
 * the possible values.
 */
public enum RideStatus {
    // A passenger has just created the ride; no driver assigned yet.
    REQUESTED,
    // A driver has been picked (see RideService#assignDriver) but has not
    // yet accepted the ride.
    ASSIGNED,
    // The assigned driver has accepted the ride.
    ACCEPTED,
    // The driver has picked up the passenger and the trip is underway.
    IN_PROGRESS,
    // The trip has finished; final fare and payment have been processed.
    COMPLETED,
    // The ride was cancelled before completion; this is a terminal state.
    CANCELLED
}
