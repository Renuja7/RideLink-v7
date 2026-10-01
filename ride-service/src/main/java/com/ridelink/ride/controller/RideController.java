package com.ridelink.ride.controller;

import com.ridelink.ride.dto.AssignDriverRequest;
import com.ridelink.ride.dto.RideRequest;
import com.ridelink.ride.dto.RideResponse;
import com.ridelink.ride.service.RideService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller exposing the Ride Management Service's public API under
 * /api/rides. This is the only entry point external clients (Swagger UI,
 * Postman, other services) use to interact with rides — all business rules
 * (status transitions, driver assignment, fare handling) live in
 * RideService, not here. The controller's job is just HTTP plumbing:
 * mapping URLs/verbs to service calls and choosing the response status code.
 */
@RestController
@RequestMapping("/api/rides")
@RequiredArgsConstructor
@Tag(name = "Rides", description = "Ride request creation, assignment and lifecycle management")
public class RideController {

    private final RideService rideService;

    /**
     * Creates a new ride request for a passenger (status starts as
     * REQUESTED). Internally this also asks Fare & Payment Service for an
     * upfront fare estimate.
     *
     * @param request pickup/destination locations, distance and passenger id
     *                (validated via @Valid — missing/blank fields are
     *                rejected with 400 Bad Request before reaching the service).
     * @return 201 Created with the newly created ride.
     */
    @PostMapping
    @Operation(summary = "Create a new ride request")
    public ResponseEntity<RideResponse> createRide(@Valid @RequestBody RideRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(rideService.createRide(request));
    }

    /**
     * Assigns a driver to a ride, moving it from REQUESTED to ASSIGNED.
     * A specific driverId can be supplied, otherwise driver-service is asked
     * for the list of currently available drivers and the first one is
     * picked (the assignment's documented simple assignment rule).
     *
     * @param id      the ride to assign a driver to.
     * @param request optional body carrying a specific driverId; if the
     *                request body is omitted entirely, an empty request is
     *                used so the "pick first available driver" path runs.
     * @return 200 OK with the ride now in ASSIGNED status.
     */
    @PutMapping("/{id}/assign")
    @Operation(summary = "Assign an eligible driver to a ride (REQUESTED -> ASSIGNED)")
    public ResponseEntity<RideResponse> assignDriver(@PathVariable String id,
                                                       @RequestBody(required = false) AssignDriverRequest request) {
        // No request body at all is a valid call (assign-me-any-driver), so we
        // substitute an empty AssignDriverRequest rather than passing null down.
        return ResponseEntity.ok(rideService.assignDriver(id, request != null ? request : new AssignDriverRequest()));
    }

    /**
     * Driver accepts a ride that has been assigned to them, moving it from
     * ASSIGNED to ACCEPTED.
     *
     * @param id the ride being accepted.
     * @return 200 OK with the ride now in ACCEPTED status.
     */
    @PutMapping("/{id}/accept")
    @Operation(summary = "Driver accepts an assigned ride (ASSIGNED -> ACCEPTED)")
    public ResponseEntity<RideResponse> acceptRide(@PathVariable String id) {
        return ResponseEntity.ok(rideService.acceptRide(id));
    }

    /**
     * Driver starts an accepted ride (pickup has happened), moving it from
     * ACCEPTED to IN_PROGRESS.
     *
     * @param id the ride being started.
     * @return 200 OK with the ride now in IN_PROGRESS status.
     */
    @PutMapping("/{id}/start")
    @Operation(summary = "Driver starts an accepted ride (ACCEPTED -> IN_PROGRESS)")
    public ResponseEntity<RideResponse> startRide(@PathVariable String id) {
        return ResponseEntity.ok(rideService.startRide(id));
    }

    /**
     * Completes a ride, moving it from IN_PROGRESS to COMPLETED, calculating
     * the final fare and triggering a simulated payment via payment-service.
     *
     * @param id the ride being completed.
     * @return 200 OK with the ride now in COMPLETED status and its final fare set.
     */
    @PutMapping("/{id}/complete")
    @Operation(summary = "Complete a ride and trigger simulated payment (IN_PROGRESS -> COMPLETED)")
    public ResponseEntity<RideResponse> completeRide(@PathVariable String id) {
        return ResponseEntity.ok(rideService.completeRide(id));
    }

    /**
     * Cancels a ride. Allowed from any status before COMPLETED (once a ride
     * is COMPLETED it cannot be cancelled) — enforced by RideService's
     * status transition rules.
     *
     * @param id the ride being cancelled.
     * @return 200 OK with the ride now in CANCELLED status.
     */
    @PutMapping("/{id}/cancel")
    @Operation(summary = "Cancel a ride prior to completion")
    public ResponseEntity<RideResponse> cancelRide(@PathVariable String id) {
        return ResponseEntity.ok(rideService.cancelRide(id));
    }

    /**
     * Retrieves a single ride by its id, whatever its current status.
     *
     * @param id the ride to look up.
     * @return 200 OK with the ride details, or 404 Not Found if no such ride exists.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Retrieve a ride by id")
    public ResponseEntity<RideResponse> getRideById(@PathVariable String id) {
        return ResponseEntity.ok(rideService.getRideById(id));
    }
}
