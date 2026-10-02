package com.ridelink.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/fare/estimate.
 * Carries the pickup and destination (fictional place names, per the
 * assignment's simulated-context rules) plus the distance between them,
 * which is the only value actually used by the fare formula. The validation
 * annotations here feed straight into GlobalExceptionHandler's 400 responses
 * for the "invalid input" negative scenario.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateRequest {

    @NotBlank(message = "pickupLocation is required")
    private String pickupLocation;

    @NotBlank(message = "destinationLocation is required")
    private String destinationLocation;

    // Distance drives the fare calculation, so it must be present and > 0.
    @NotNull(message = "distanceKm is required")
    @Positive(message = "distanceKm must be positive")
    private Double distanceKm;
}
