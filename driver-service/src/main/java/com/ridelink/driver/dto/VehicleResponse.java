package com.ridelink.driver.dto;

import com.ridelink.driver.entity.VehicleType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * API-facing view of a registered vehicle, returned by VehicleController.
 * Kept separate from the {@link com.ridelink.driver.entity.Vehicle} JPA
 * entity so the persistence model can evolve independently of the API.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VehicleResponse {
    private String id;
    private String driverId; // the owning driver's internal id
    private String vehicleNumber;
    private String vehicleModel;
    private VehicleType vehicleType;
}
