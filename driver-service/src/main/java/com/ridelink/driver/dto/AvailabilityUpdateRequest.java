package com.ridelink.driver.dto;

import com.ridelink.driver.entity.AvailabilityStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/drivers/{id}/availability.
 * Carries the new AvailabilityStatus a driver (or admin) wants to set - this
 * is what Ride Service's assignment logic ultimately depends on being kept
 * accurate.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AvailabilityUpdateRequest {

    @NotNull(message = "availabilityStatus is required") // must be one of AVAILABLE/UNAVAILABLE/ON_TRIP
    private AvailabilityStatus availabilityStatus;
}
