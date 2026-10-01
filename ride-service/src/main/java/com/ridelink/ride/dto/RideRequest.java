package com.ridelink.ride.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/rides (creating a new ride request).
 * The @NotNull/@NotBlank annotations are enforced by Spring's validation
 * framework via @Valid on the controller method — an invalid request never
 * reaches RideService; it is rejected with 400 Bad Request and a message
 * built from these annotations' text (see GlobalExceptionHandler's handling
 * of MethodArgumentNotValidException).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RideRequest {

    // Stable numeric id of the passenger from account-service — no foreign
    // key here since each service owns its own database.
    @NotNull(message = "passengerId is required")
    private String passengerId;

    @NotBlank(message = "pickupLocation is required")
    private String pickupLocation;

    @NotBlank(message = "destinationLocation is required")
    private String destinationLocation;

    // Simulated distance in kilometres, used both for the fallback fare
    // formula and as an input to payment-service's fare estimate.
    @NotNull(message = "distanceKm is required")
    private Double distanceKm;
}
