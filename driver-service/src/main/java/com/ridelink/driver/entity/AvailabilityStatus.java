package com.ridelink.driver.entity;

/**
 * The three operational states a driver can be in.
 * AVAILABLE drivers are the ones returned by GET /api/drivers/available and
 * therefore eligible to be assigned a new ride by Ride Management Service;
 * ON_TRIP represents a driver currently carrying out an assigned ride;
 * UNAVAILABLE is the default/off-duty state (see Driver entity's default).
 */
public enum AvailabilityStatus {
    AVAILABLE,
    UNAVAILABLE,
    ON_TRIP
}
