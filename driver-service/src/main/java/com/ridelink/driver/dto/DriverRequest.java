package com.ridelink.driver.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/drivers - creates a driver operational profile.
 * userId links this profile back to an account created in Account Service,
 * but it is a plain stable identifier, not a JPA foreign key: Driver Service
 * never queries Account Service's database directly, each service owns its
 * own persistence boundary.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverRequest {

    @NotNull(message = "userId is required") // reference to the account owner; not a DB foreign key
    private String userId;

    private String currentLocation; // optional starting simulated location

    private String serviceArea; // optional starting declared operating zone
}
