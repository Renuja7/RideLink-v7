package com.ridelink.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response returned by payment-service's fare-estimate endpoint.
 * ride-service uses this to store an upfront fare on a newly created ride;
 * if the call to payment-service fails, ride-service computes the same
 * documented formula (200 + distanceKm * 50) locally instead (see
 * RideService#createRide).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateResponse {
    private BigDecimal estimatedFare;
}
