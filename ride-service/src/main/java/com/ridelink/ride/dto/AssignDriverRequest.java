package com.ridelink.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/rides/{id}/assign.
 * driverId is optional: if it is left null, RideService picks the first
 * available driver returned by driver-service instead of a caller-specified one.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AssignDriverRequest {
    // Optional: a specific driver to assign. Null means "let the system pick one".
    private String driverId;
}
