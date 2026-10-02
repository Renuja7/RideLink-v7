package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit tests for FareService's documented fare formula
 * (fare = baseFare + distanceKm * ratePerKm). Uses ReflectionTestUtils to
 * inject baseFare/ratePerKm directly since they are normally set from
 * application.yml via @Value.
 */
@ExtendWith(MockitoExtension.class)
class FareServiceTest {

    @InjectMocks
    private FareService fareService;

    @Test
    void estimateFare_shouldApplyDocumentedFormula() {
        ReflectionTestUtils.setField(fareService, "baseFare", 200.0);
        ReflectionTestUtils.setField(fareService, "ratePerKm", 50.0);

        FareEstimateRequest request = new FareEstimateRequest("Colombo Fort", "Nugegoda", 10.0);

        FareEstimateResponse response = fareService.estimateFare(request);

        // fare = 200 + (10 * 50) = 700
        assertEquals(0, BigDecimal.valueOf(700.00).compareTo(response.getEstimatedFare()));
    }

    @Test
    void calculateFare_shouldHandleFractionalDistance() {
        ReflectionTestUtils.setField(fareService, "baseFare", 200.0);
        ReflectionTestUtils.setField(fareService, "ratePerKm", 50.0);

        // fare = 200 + (2.5 * 50) = 325
        BigDecimal fare = fareService.calculateFare(2.5);

        assertEquals(0, BigDecimal.valueOf(325.00).compareTo(fare));
    }
}
