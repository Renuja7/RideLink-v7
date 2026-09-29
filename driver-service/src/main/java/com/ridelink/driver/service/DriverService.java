package com.ridelink.driver.service;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.entity.AvailabilityStatus;
import com.ridelink.driver.entity.Driver;
import com.ridelink.driver.exception.DuplicateResourceException;
import com.ridelink.driver.exception.ResourceNotFoundException;
import com.ridelink.driver.repository.DriverRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Business logic for driver operational profiles.
 * Holds all the rules DriverController needs (duplicate prevention, lookup
 * failures, entity-to-DTO mapping) so the controller can stay a thin HTTP
 * adapter. This is where the "one profile per userId" and "must exist to
 * update" business rules actually live.
 */
@Service
@RequiredArgsConstructor
public class DriverService {

    private final DriverRepository driverRepository;

    /**
     * Creates a new driver operational profile.
     * Enforces the business rule that each account (userId) may only have
     * one driver profile - attempting to register twice results in a
     * DuplicateResourceException (mapped to HTTP 409 by the global handler).
     * New drivers always start UNAVAILABLE regardless of what was requested.
     */
    @Transactional
    public DriverResponse createDriver(DriverRequest request) {
        // Guard against duplicate profiles for the same account reference.
        if (driverRepository.findByUserId(request.getUserId()).isPresent()) {
            throw new DuplicateResourceException("Driver profile already exists for userId: " + request.getUserId());
        }

        Driver driver = Driver.builder()
                .userId(request.getUserId())
                .currentLocation(request.getCurrentLocation())
                .serviceArea(request.getServiceArea())
                .availabilityStatus(AvailabilityStatus.UNAVAILABLE) // force a safe default; ignores any status the caller might have tried to set
                .build();

        Driver saved = driverRepository.save(driver);
        return toResponse(saved);
    }

    /**
     * Updates a driver's availability status.
     * This is the field Ride Service's driver-matching logic ultimately
     * relies on via GET /api/drivers/available, so keeping it accurate here
     * matters for the whole ride assignment workflow.
     */
    @Transactional
    public DriverResponse updateAvailability(String id, AvailabilityUpdateRequest request) {
        Driver driver = findDriverOrThrow(id); // 404s if the driver id doesn't exist
        driver.setAvailabilityStatus(request.getAvailabilityStatus());
        return toResponse(driverRepository.save(driver));
    }

    /**
     * Updates a driver's simulated current location.
     * Expected to be called often (e.g. as a driver moves), independent of
     * the much less frequently updated service area.
     */
    @Transactional
    public DriverResponse updateLocation(String id, LocationUpdateRequest request) {
        Driver driver = findDriverOrThrow(id);
        driver.setCurrentLocation(request.getCurrentLocation());
        return toResponse(driverRepository.save(driver));
    }

    /**
     * Updates a driver's declared service area.
     * Kept as its own operation (separate from location/availability) so the
     * brief's explicit "service area" requirement is satisfied independently.
     */
    @Transactional
    public DriverResponse updateServiceArea(String id, ServiceAreaUpdateRequest request) {
        Driver driver = findDriverOrThrow(id);
        driver.setServiceArea(request.getServiceArea());
        return toResponse(driverRepository.save(driver));
    }

    /**
     * Returns every driver currently marked AVAILABLE.
     * Backs the interservice contract Ride Management Service depends on
     * (via a Feign client) to find a driver to assign to a new ride - the
     * response shape here must stay stable for that integration to keep working.
     */
    public List<DriverResponse> getAvailableDrivers() {
        return driverRepository.findByAvailabilityStatus(AvailabilityStatus.AVAILABLE).stream()
                .map(this::toResponse)
                .toList();
    }

    /** Retrieves a single driver profile, throwing 404 if it doesn't exist. */
    public DriverResponse getDriverById(String id) {
        return toResponse(findDriverOrThrow(id));
    }

    // Shared lookup helper: centralises the "driver must exist" business rule
    // so every mutating/read operation above fails the same consistent way.
    private Driver findDriverOrThrow(String id) {
        return driverRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Driver not found with id: " + id));
    }

    // Maps the JPA entity to its public API DTO, keeping the persistence
    // model decoupled from what's exposed over HTTP.
    private DriverResponse toResponse(Driver driver) {
        return DriverResponse.builder()
                .id(driver.getId())
                .userId(driver.getUserId())
                .availabilityStatus(driver.getAvailabilityStatus())
                .currentLocation(driver.getCurrentLocation())
                .serviceArea(driver.getServiceArea())
                .build();
    }
}
