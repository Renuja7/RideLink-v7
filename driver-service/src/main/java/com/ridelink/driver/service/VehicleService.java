package com.ridelink.driver.service;

import com.ridelink.driver.dto.VehicleRequest;
import com.ridelink.driver.dto.VehicleResponse;
import com.ridelink.driver.entity.Vehicle;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.repository.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Business logic for vehicle registration.
 * Kept separate from {@link DriverService} because vehicles are a distinct
 * business concern from a driver's operational state - this mirrors the
 * Driver/Vehicle entity split (Single Responsibility Principle).
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;

    /**
     * Registers a new vehicle for a driver.
     * Enforces the business rule that a vehicle number can only be
     * registered once - a duplicate attempt raises DuplicateResourceException
     * (mapped to HTTP 409 by the global handler).
     */
    @Transactional
    public VehicleResponse registerVehicle(VehicleRequest request) {
        // Guard against the same plate/number being registered more than once.
        if (vehicleRepository.findByVehicleNumber(request.getVehicleNumber()).isPresent()) {
            throw new DuplicateResourceException("Vehicle already registered: " + request.getVehicleNumber());
        }

        Vehicle vehicle = Vehicle.builder()
                .driverId(request.getDriverId())
                .vehicleNumber(request.getVehicleNumber())
                .vehicleModel(request.getVehicleModel())
                .vehicleType(request.getVehicleType())
                .build();

        Vehicle saved = vehicleRepository.save(vehicle);
        return toResponse(saved);
    }

    // Maps the JPA entity to its public API DTO, keeping the persistence
    // model decoupled from what's exposed over HTTP.
    private VehicleResponse toResponse(Vehicle vehicle) {
        return VehicleResponse.builder()
                .id(vehicle.getId())
                .driverId(vehicle.getDriverId())
                .vehicleNumber(vehicle.getVehicleNumber())
                .vehicleModel(vehicle.getVehicleModel())
                .vehicleType(vehicle.getVehicleType())
                .build();
    }
}
