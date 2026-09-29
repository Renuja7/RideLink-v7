package com.ridelink.driver.entity;

/**
 * The categories of vehicle a driver can register.
 * Kept as a simple enum (rather than a free-text field) so downstream
 * services and fare calculations can rely on a fixed, known set of values.
 */
public enum VehicleType {
    CAR,
    VAN,
    TUK,
    MOTORBIKE
}
