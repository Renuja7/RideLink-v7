package com.ridelink.driver.controller;

import com.ridelink.driver.dto.*;
import com.ridelink.driver.service.DriverService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST API for managing a driver's operational profile: availability status,
 * simulated current location and declared service area.
 * This is the primary HTTP surface for Member 2's Driver & Vehicle Service.
 * Vehicle registration lives in {@link VehicleController} instead, because a
 * driver's operational state changes far more often than their vehicle
 * details, so the two are kept as separate resources/entities.
 */
@RestController
@RequestMapping("/api/drivers")
@RequiredArgsConstructor
@Tag(name = "Drivers", description = "Driver operational profile management")
public class DriverController {

    private final DriverService driverService; // delegates all business logic; controller stays thin

    /**
     * Creates a new driver operational profile for an existing account.
     * The request only carries a userId (a loose reference to a row owned by
     * Account Service, not a foreign key) plus optional starting location and
     * service area. Returns 201 Created with the new profile on success.
     */
    @PostMapping
    @Operation(summary = "Create a driver operational profile")
    public ResponseEntity<DriverResponse> createDriver(@Valid @RequestBody DriverRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(driverService.createDriver(request));
    }

    /**
     * Updates a driver's availability status (AVAILABLE / UNAVAILABLE / ON_TRIP).
     * Drivers or admins toggle this to signal whether they can be matched to
     * a new ride request.
     */
    @PutMapping("/{id}/availability")
    @Operation(summary = "Update a driver's availability status")
    public ResponseEntity<DriverResponse> updateAvailability(@PathVariable String id,
                                                               @Valid @RequestBody AvailabilityUpdateRequest request) {
        return ResponseEntity.ok(driverService.updateAvailability(id, request));
    }

    /**
     * Updates the driver's simulated live/current location.
     * This is expected to change frequently (e.g. every few minutes while a
     * driver is on shift), unlike the broader, rarely-changing service area.
     */
    @PutMapping("/{id}/location")
    @Operation(summary = "Update a driver's current simulated location")
    public ResponseEntity<DriverResponse> updateLocation(@PathVariable String id,
                                                           @Valid @RequestBody LocationUpdateRequest request) {
        return ResponseEntity.ok(driverService.updateLocation(id, request));
    }

    /**
     * Updates the driver's declared service area (the broader zone they
     * operate in, e.g. a city or region name).
     * This endpoint was added specifically to satisfy the assignment brief's
     * requirement that "service area" be updatable independently of
     * availability and live location - it behaves more like a driver
     * preference than a moment-to-moment position.
     */
    @PutMapping("/{id}/service-area")
    @Operation(summary = "Update a driver's declared service area")
    public ResponseEntity<DriverResponse> updateServiceArea(@PathVariable String id,
                                                              @Valid @RequestBody ServiceAreaUpdateRequest request) {
        return ResponseEntity.ok(driverService.updateServiceArea(id, request));
    }

    /**
     * Returns every driver currently marked AVAILABLE.
     * This endpoint is one of the two required interservice interactions for
     * the whole assignment: Ride Management Service calls it through a Feign
     * client when it needs to assign a driver to a new ride request. It is
     * only "authenticated" (see SecurityConfig) rather than role-restricted,
     * because both a passenger's own flow and Ride Service acting on a
     * passenger's behalf need to be able to read it - keep this contract
     * (path, response shape) stable since another service depends on it.
     */
    @GetMapping("/available")
    @Operation(summary = "Get all currently available drivers")
    public ResponseEntity<List<DriverResponse>> getAvailableDrivers() {
        return ResponseEntity.ok(driverService.getAvailableDrivers());
    }

    /**
     * Retrieves a single driver's operational profile by its internal id.
     * Throws a 404 (via ResourceNotFoundException) when no such driver exists.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a driver profile by id")
    public ResponseEntity<DriverResponse> getDriverById(@PathVariable String id) {
        return ResponseEntity.ok(driverService.getDriverById(id));
    }
}
