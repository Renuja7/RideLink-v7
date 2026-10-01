package com.ridelink.ride.service;

import com.ridelink.ride.client.DriverServiceClient;
import com.ridelink.ride.client.PaymentServiceClient;
import com.ridelink.ride.dto.*;
import com.ridelink.ride.entity.Ride;
import com.ridelink.ride.entity.RideStatus;
import com.ridelink.ride.exception.InvalidStatusTransitionException;
import com.ridelink.ride.exception.NoAvailableDriverException;
import com.ridelink.ride.repository.RideRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for RideService covering the ride lifecycle, driver assignment
 * and the Feign fallback/failure behaviour, including two of the assignment's
 * required negative scenarios (no available driver, invalid status transition).
 * The Feign clients are mocked so no real network call to driver-service or
 * payment-service happens here.
 */
@ExtendWith(MockitoExtension.class)
class RideServiceTest {

    // Mocked collaborators — Mockito injects these into rideService below via
    // @InjectMocks, so tests can control their behaviour without a real
    // database or real driver-service/payment-service calls.
    @Mock
    private RideRepository rideRepository;

    @Mock
    private DriverServiceClient driverServiceClient;

    @Mock
    private PaymentServiceClient paymentServiceClient;

    @InjectMocks
    private RideService rideService;

    private Ride ride;

    // Runs before every test: sets up a fresh ride in REQUESTED status so
    // each test starts from a known, predictable state.
    @BeforeEach
    void setUp() {
        ride = Ride.builder()
                .id("1")
                .passengerId("10")
                .pickupLocation("Colombo Fort")
                .destinationLocation("Nugegoda")
                .estimatedFare(BigDecimal.valueOf(700))
                .status(RideStatus.REQUESTED)
                .build();
    }

    // Happy path: payment-service responds normally, so its estimate is used as-is.
    @Test
    void createRide_shouldUseFareServiceEstimate_whenAvailable() {
        RideRequest request = new RideRequest("10", "Colombo Fort", "Nugegoda", 10.0);

        when(paymentServiceClient.getFareEstimate(any(FareEstimateRequest.class)))
                .thenReturn(new FareEstimateResponse(BigDecimal.valueOf(700)));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse response = rideService.createRide(request);

        assertEquals(RideStatus.REQUESTED, response.getStatus());
        assertEquals(BigDecimal.valueOf(700), response.getEstimatedFare());
    }

    // Verifies the graceful-degradation path: when payment-service throws, ride
    // creation still succeeds using the local fallback fare formula.
    @Test
    void createRide_shouldFallBackToLocalFormula_whenPaymentServiceUnavailable() {
        RideRequest request = new RideRequest("10", "Colombo Fort", "Nugegoda", 10.0);

        when(paymentServiceClient.getFareEstimate(any(FareEstimateRequest.class)))
                .thenThrow(new RuntimeException("service unavailable"));
        when(rideRepository.save(any(Ride.class))).thenReturn(ride);

        RideResponse response = rideService.createRide(request);

        assertNotNull(response);
        verify(rideRepository).save(any(Ride.class));
    }

    // Verifies the "pick first available driver" simple assignment rule.
    @Test
    void assignDriver_shouldAssignFirstAvailableDriver_whenNoneSpecified() {
        DriverResponse available = new DriverResponse("5", "50", "AVAILABLE", "Colombo", "Colombo District");

        when(rideRepository.findById("1")).thenReturn(Optional.of(ride));
        when(driverServiceClient.getAvailableDrivers()).thenReturn(List.of(available));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        RideResponse response = rideService.assignDriver("1", new AssignDriverRequest());

        assertEquals(RideStatus.ASSIGNED, response.getStatus());
        assertEquals("5", response.getDriverId());
    }

    // Negative scenario: no available drivers -> NoAvailableDriverException (409).
    @Test
    void assignDriver_shouldThrow_whenNoDriversAvailable() {
        when(rideRepository.findById("1")).thenReturn(Optional.of(ride));
        when(driverServiceClient.getAvailableDrivers()).thenReturn(Collections.emptyList());

        assertThrows(NoAvailableDriverException.class, () -> rideService.assignDriver("1", new AssignDriverRequest()));
    }

    // Negative scenario: assigning a ride that's already ASSIGNED is an
    // illegal transition -> InvalidStatusTransitionException (400).
    @Test
    void assignDriver_shouldThrow_whenRideAlreadyAssigned() {
        ride.setStatus(RideStatus.ASSIGNED);
        when(rideRepository.findById("1")).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class,
                () -> rideService.assignDriver("1", new AssignDriverRequest("5")));
    }

    // Negative scenario: can't complete a ride that's still REQUESTED
    // (skips ASSIGNED/ACCEPTED/IN_PROGRESS) -> InvalidStatusTransitionException.
    @Test
    void completeRide_shouldThrow_whenRideNotInProgress() {
        ride.setStatus(RideStatus.REQUESTED);
        when(rideRepository.findById("1")).thenReturn(Optional.of(ride));

        assertThrows(InvalidStatusTransitionException.class, () -> rideService.completeRide("1"));
    }

    // Verifies cancellation is allowed from a non-terminal state (ACCEPTED here).
    @Test
    void cancelRide_shouldSucceed_whenRideNotYetCompleted() {
        ride.setStatus(RideStatus.ACCEPTED);
        when(rideRepository.findById("1")).thenReturn(Optional.of(ride));
        when(rideRepository.save(any(Ride.class))).thenAnswer(inv -> inv.getArgument(0));

        RideResponse response = rideService.cancelRide("1");

        assertEquals(RideStatus.CANCELLED, response.getStatus());
    }
}
