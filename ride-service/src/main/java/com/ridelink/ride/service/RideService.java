package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.PaymentServiceClient;
import com.ridelink.ride.dto.*;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.entity.RideStatus;
import com.ridelink.ride.exception.InvalidStatusTransitionException;
import com.ridelink.ride.exception.NoAvailableDriverException;
import com.ridelink.ride.exception.ResourceNotFoundException;
import com.ridelink.ride.repository.RideRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;

/**
 * Core business-logic layer for the Ride Management Service. Everything the
 * assignment brief calls "ride request creation; pickup and destination
 * details; driver assignment; ride status lifecycle; acceptance, start,
 * completion and cancellation; ride retrieval" lives here — RideController
 * is just a thin HTTP wrapper around these methods.
 *
 * This class is also where the two required interservice interactions
 * happen: it calls driver-service (via DriverServiceClient) to find an
 * available driver, and payment-service (via PaymentServiceClient) to
 * estimate fares and record simulated payments.
 */
@Service
@RequiredArgsConstructor
public class RideService {

    private final RideRepository rideRepository;
    private final DriverServiceClient driverServiceClient;
    private final PaymentServiceClient paymentServiceClient;

    /**
     * Documented valid ride status transitions:
     * REQUESTED -> ASSIGNED -> ACCEPTED -> IN_PROGRESS -> COMPLETED
     * Cancellation is allowed from any state prior to COMPLETED.
     *
     * Design choice: this is expressed as a single explicit lookup table
     * (an EnumMap of RideStatus -> the EnumSet of statuses reachable from
     * it) instead of a chain of scattered if/else checks spread across each
     * status-changing method. That keeps the entire lifecycle rule visible
     * in one place, makes it trivial to see at a glance what is/isn't
     * allowed, and means every transition method (assignDriver, acceptRide,
     * startRide, completeRide, cancelRide) can share the exact same
     * validateTransition(...) check below rather than duplicating logic.
     */
    private static final Map<RideStatus, EnumSet<RideStatus>> VALID_TRANSITIONS = new EnumMap<>(RideStatus.class);

    // Static initializer: this table is built once, when the class is
    // loaded, and then only ever read from (never mutated) at request time.
    static {
        // From REQUESTED you can move forward to ASSIGNED, or cancel outright.
        VALID_TRANSITIONS.put(RideStatus.REQUESTED, EnumSet.of(RideStatus.ASSIGNED, RideStatus.CANCELLED));
        // From ASSIGNED the driver can accept, or the ride can still be cancelled.
        VALID_TRANSITIONS.put(RideStatus.ASSIGNED, EnumSet.of(RideStatus.ACCEPTED, RideStatus.CANCELLED));
        // From ACCEPTED the driver can start the trip, or it can be cancelled.
        VALID_TRANSITIONS.put(RideStatus.ACCEPTED, EnumSet.of(RideStatus.IN_PROGRESS, RideStatus.CANCELLED));
        // From IN_PROGRESS the trip can only finish or be cancelled.
        VALID_TRANSITIONS.put(RideStatus.IN_PROGRESS, EnumSet.of(RideStatus.COMPLETED, RideStatus.CANCELLED));
        // COMPLETED and CANCELLED are terminal states: nothing may follow them,
        // which is exactly why a completed ride can never be cancelled or
        // re-completed, and a cancelled ride can never be revived.
        VALID_TRANSITIONS.put(RideStatus.COMPLETED, EnumSet.noneOf(RideStatus.class));
        VALID_TRANSITIONS.put(RideStatus.CANCELLED, EnumSet.noneOf(RideStatus.class));
    }

    /**
     * Creates a new ride request for a passenger, starting in REQUESTED
     * status, and attaches an upfront fare estimate obtained from
     * Fare & Payment Service.
     *
     * @param request the pickup/destination locations, distance and passenger id.
     * @return the newly created and persisted ride, as a client-facing DTO.
     */
    @Transactional
    public RideResponse createRide(RideRequest request) {
        BigDecimal estimatedFare;
        try {
            // Synchronous REST call (via Feign) to payment-service to get an
            // authoritative fare estimate using its documented calculation rule.
            FareEstimateResponse fareEstimate = paymentServiceClient.getFareEstimate(
                    new FareEstimateRequest(request.getPickupLocation(), request.getDestinationLocation(), request.getDistanceKm()));
            estimatedFare = fareEstimate.getEstimatedFare();
        } catch (Exception ex) {
            // Graceful degradation: if payment-service is down/slow/erroring,
            // don't block ride creation — fall back to the SAME documented
            // formula (200 base fare + 50 per km) computed locally, so the
            // passenger still sees a reasonable estimate. This is a deliberate
            // design choice: ride creation is more time-sensitive than getting
            // the exact fare from the authoritative service right away.
            estimatedFare = BigDecimal.valueOf(200 + (request.getDistanceKm() * 50));
        }

        Ride ride = Ride.builder()
                .passengerId(request.getPassengerId())
                .pickupLocation(request.getPickupLocation())
                .destinationLocation(request.getDestinationLocation())
                .estimatedFare(estimatedFare)
                .status(RideStatus.REQUESTED)
                .build();

        return toResponse(rideRepository.save(ride));
    }

    /**
     * Assigns a driver to a ride, transitioning it from REQUESTED to
     * ASSIGNED. If the caller specifies a driverId, that driver is used
     * as-is (no cross-check against driver-service is done here); otherwise
     * driver-service is asked for the list of currently available drivers
     * and the first one returned is picked — a simple, documented assignment
     * rule (not an optimisation like nearest-driver matching).
     *
     * @param rideId  the ride to assign a driver to.
     * @param request optionally carries a specific driverId to assign.
     * @return the ride, now in ASSIGNED status with driverId set.
     * @throws NoAvailableDriverException if no driverId was supplied and
     *         driver-service reports zero available drivers — this is a
     *         required negative scenario, since assignment genuinely cannot
     *         proceed without knowing driver availability.
     */
    @Transactional
    public RideResponse assignDriver(String rideId, AssignDriverRequest request) {
        Ride ride = findRideOrThrow(rideId);
        // Enforces the lifecycle rule: only a REQUESTED ride can become ASSIGNED.
        validateTransition(ride.getStatus(), RideStatus.ASSIGNED);

        String driverId = request.getDriverId();
        if (driverId == null) {
            // Interservice call to driver-service (via Feign) to find who's free.
            // Deliberately NOT wrapped in try/catch: unlike the fare estimate
            // above, there is no sensible local fallback for "which drivers are
            // available right now" — that data only exists in driver-service.
            // If this call fails or returns nothing, we must fail loudly rather
            // than silently assigning a driver we can't actually confirm is free.
            List<DriverResponse> availableDrivers = driverServiceClient.getAvailableDrivers();
            if (availableDrivers.isEmpty()) {
                throw new NoAvailableDriverException("No available drivers found for assignment");
            }
            // Simple, documented assignment rule: just take the first available
            // driver in the list (no distance/rating optimisation for this assignment).
            driverId = availableDrivers.get(0).getId();
        }

        ride.setDriverId(driverId);
        ride.setStatus(RideStatus.ASSIGNED);
        return toResponse(rideRepository.save(ride));
    }

    /**
     * Driver accepts a ride that was assigned to them, transitioning it from
     * ASSIGNED to ACCEPTED.
     *
     * @param rideId the ride being accepted.
     * @return the ride, now in ACCEPTED status.
     * @throws InvalidStatusTransitionException if the ride is not currently ASSIGNED.
     */
    @Transactional
    public RideResponse acceptRide(String rideId) {
        Ride ride = findRideOrThrow(rideId);
        validateTransition(ride.getStatus(), RideStatus.ACCEPTED);
        ride.setStatus(RideStatus.ACCEPTED);
        return toResponse(rideRepository.save(ride));
    }

    /**
     * Driver starts an accepted ride (pickup complete, trip underway),
     * transitioning it from ACCEPTED to IN_PROGRESS.
     *
     * @param rideId the ride being started.
     * @return the ride, now in IN_PROGRESS status.
     * @throws InvalidStatusTransitionException if the ride is not currently ACCEPTED.
     */
    @Transactional
    public RideResponse startRide(String rideId) {
        Ride ride = findRideOrThrow(rideId);
        validateTransition(ride.getStatus(), RideStatus.IN_PROGRESS);
        ride.setStatus(RideStatus.IN_PROGRESS);
        return toResponse(rideRepository.save(ride));
    }

    /**
     * Completes a ride: transitions it from IN_PROGRESS to COMPLETED,
     * calculates/records the final fare, and triggers a simulated payment
     * via Fare & Payment Service.
     *
     * @param rideId the ride being completed.
     * @return the ride, now in COMPLETED status with finalFare set.
     * @throws InvalidStatusTransitionException if the ride is not currently IN_PROGRESS.
     */
    @Transactional
    public RideResponse completeRide(String rideId) {
        Ride ride = findRideOrThrow(rideId);
        // Enforces the lifecycle rule: only an IN_PROGRESS ride can be completed —
        // this is what makes "complete a REQUESTED ride" a rejected negative scenario.
        validateTransition(ride.getStatus(), RideStatus.COMPLETED);

        // Simplified final-fare rule: reuse the estimated fare captured at
        // creation time (or a flat 200 fallback if somehow missing), rather
        // than recomputing from live trip data.
        BigDecimal finalFare = ride.getEstimatedFare() != null ? ride.getEstimatedFare() : BigDecimal.valueOf(200);
        ride.setFinalFare(finalFare);
        ride.setStatus(RideStatus.COMPLETED);
        // The ride's completion is persisted immediately — completion does not
        // wait on the payment call below to succeed.
        Ride saved = rideRepository.save(ride);

        try {
            // Interservice call to payment-service to record a simulated payment
            // for this completed ride.
            paymentServiceClient.createPayment(new CreatePaymentRequest(saved.getId(), finalFare, "CASH"));
        } catch (Exception ignored) {
            // Graceful degradation, opposite asymmetry from assignDriver(): a
            // completed ride is already a real, meaningful outcome on its own,
            // so we don't want a flaky/slow payment-service call to roll back or
            // block ride completion. Payment failures are handled/retried by
            // payment-service; ride completion still stands.
        }

        return toResponse(saved);
    }

    /**
     * Cancels a ride. Allowed from any non-terminal status (REQUESTED,
     * ASSIGNED, ACCEPTED, IN_PROGRESS) but never once a ride is COMPLETED.
     *
     * @param rideId the ride being cancelled.
     * @return the ride, now in CANCELLED status.
     * @throws InvalidStatusTransitionException if the ride is already COMPLETED (or CANCELLED).
     */
    @Transactional
    public RideResponse cancelRide(String rideId) {
        Ride ride = findRideOrThrow(rideId);
        validateTransition(ride.getStatus(), RideStatus.CANCELLED);
        ride.setStatus(RideStatus.CANCELLED);
        return toResponse(rideRepository.save(ride));
    }

    /**
     * Retrieves a single ride by id.
     *
     * @param rideId the ride to look up.
     * @return the ride, in whatever status it currently holds.
     * @throws ResourceNotFoundException if no ride exists with that id.
     */
    public RideResponse getRideById(String rideId) {
        return toResponse(findRideOrThrow(rideId));
    }

    /**
     * Checks the ride status lifecycle rule (the VALID_TRANSITIONS map)
     * before any status change is applied, and rejects the change if it
     * isn't a legal move — this single method is what every lifecycle
     * method (assignDriver, acceptRide, startRide, completeRide, cancelRide)
     * relies on, so the rule is enforced consistently everywhere.
     *
     * @throws InvalidStatusTransitionException if target is not reachable from current.
     */
    private void validateTransition(RideStatus current, RideStatus target) {
        if (!VALID_TRANSITIONS.getOrDefault(current, EnumSet.noneOf(RideStatus.class)).contains(target)) {
            throw new InvalidStatusTransitionException(
                    String.format("Cannot transition ride from %s to %s", current, target));
        }
    }

    /**
     * Looks up a ride by id or throws, so every public method above gets a
     * consistent 404-style failure for a missing ride without repeating the
     * orElseThrow(...) boilerplate.
     */
    private Ride findRideOrThrow(String id) {
        return rideRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Ride not found with id: " + id));
    }

    /**
     * Converts the internal JPA entity into the public-facing RideResponse
     * DTO returned by the API, so persistence details never leak directly
     * into the HTTP contract.
     */
    private RideResponse toResponse(Ride ride) {
        return RideResponse.builder()
                .id(ride.getId())
                .passengerId(ride.getPassengerId())
                .driverId(ride.getDriverId())
                .pickupLocation(ride.getPickupLocation())
                .destinationLocation(ride.getDestinationLocation())
                .estimatedFare(ride.getEstimatedFare())
                .finalFare(ride.getFinalFare())
                .status(ride.getStatus())
                .createdAt(ride.getCreatedAt())
                .build();
    }
}
