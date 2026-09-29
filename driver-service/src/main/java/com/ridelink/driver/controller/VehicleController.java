package com.ridelink.driver.controller;

import com.ridelink.driver.dto.VehicleRequest;
import com.ridelink.driver.dto.VehicleResponse;
import com.ridelink.driver.service.VehicleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST API for registering and managing vehicles.
 * Kept as a separate resource/controller from {@link DriverController}
 * because vehicle details (plate number, model, type) rarely change, unlike
 * a driver's operational state - this separation follows the Single
 * Responsibility Principle and would let one driver register multiple
 * vehicles in the future without reworking the Driver entity.
 */
@RestController
@RequestMapping("/api/vehicles")
@RequiredArgsConstructor
@Tag(name = "Vehicles", description = "Vehicle registration and management")
public class VehicleController {

    private final VehicleService vehicleService; // delegates all business logic; controller stays thin

    /**
     * Registers a new vehicle and associates it with a driver (by driverId).
     * Rejects registration if the vehicle number is already registered to
     * avoid duplicate vehicle records. Returns 201 Created on success.
     */
    @PostMapping
    @Operation(summary = "Register a vehicle for a driver")
    public ResponseEntity<VehicleResponse> registerVehicle(@Valid @RequestBody VehicleRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(vehicleService.registerVehicle(request));
    }
}
