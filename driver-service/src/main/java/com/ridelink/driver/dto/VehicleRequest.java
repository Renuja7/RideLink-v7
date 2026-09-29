package com.ridelink.driver.dto;

import com.ridelink.driver.entity.VehicleType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Request body for POST /api/vehicles - registers a vehicle for a driver.
 * driverId here IS an internal id belonging to this same service's Driver
 * entity (unlike the loose cross-service userId reference), since both
 * Driver and Vehicle are owned by Driver Service's own database.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleRequest {

    @NotNull(message = "driverId is required") // which driver this vehicle belongs to
    private String driverId;

    @NotBlank(message = "vehicleNumber is required") // must be unique across all vehicles
    private String vehicleNumber;

    @NotBlank(message = "vehicleModel is required")
    private String vehicleModel;

    @NotNull(message = "vehicleType is required")
    private VehicleType vehicleType;
}
