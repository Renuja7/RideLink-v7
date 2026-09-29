package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/drivers/{id}/location.
 * Represents a simulated live position update (place name or simulated
 * coordinates - no real maps/GPS integration is required by the brief).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class LocationUpdateRequest {

    @NotBlank(message = "currentLocation is required") // must not be null/empty
    private String currentLocation;
}
