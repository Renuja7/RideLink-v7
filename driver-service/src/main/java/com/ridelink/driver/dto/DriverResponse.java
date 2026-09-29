package com.ridelink.driver.dto;

import com.ridelink.driver.entity.AvailabilityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-facing view of a driver's operational profile, returned by all
 * DriverController endpoints. Kept separate from the {@link
 * com.ridelink.driver.entity.Driver} JPA entity so the persistence model can
 * evolve independently of the public API contract.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    private String id;
    private String userId; // loose reference to the account owner, not a foreign key
    private AvailabilityStatus availabilityStatus;
    private String currentLocation; // simulated live position, changes often
    private String serviceArea; // broader declared operating zone, changes rarely
}
