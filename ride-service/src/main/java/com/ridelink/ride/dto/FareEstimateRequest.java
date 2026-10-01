package com.ridelink.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request payload sent to payment-service's /api/fare/estimate endpoint.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateRequest {
    private String pickupLocation;
    private String destinationLocation;
    // Simulated distance in kilometres — no real map/routing service is used,
    // per the assignment's "fictional and simulated context" scope.
    private Double distanceKm;
}
