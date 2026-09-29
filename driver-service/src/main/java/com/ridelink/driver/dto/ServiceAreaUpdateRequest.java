package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for PUT /api/drivers/{id}/service-area.
 * Represents the driver's broader declared operating zone (e.g. a city or
 * region), distinct from the fine-grained, frequently-changing current
 * location - added to satisfy the brief's explicit "service area" requirement.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceAreaUpdateRequest {

    @NotBlank(message = "serviceArea is required") // must not be null/empty
    private String serviceArea;
}
