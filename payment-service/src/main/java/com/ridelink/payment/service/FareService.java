package com.ridelink.payment.service;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Owns the single documented fare calculation rule for the whole RideLink
 * platform. This is deliberately the only place the formula lives: the Ride
 * Management Service calls this service (via OpenFeign) to get a fare
 * instead of re-implementing the rule itself, so the business rule can
 * change in one place without touching other services.
 */
@Service
@RequiredArgsConstructor
public class FareService {

    // Externalised via application.yml (fare.base-fare / fare.rate-per-km)
    // rather than hard-coded, so the pricing rule can be tuned per
    // environment (e.g. for a demo) without a code change or redeploy.
    @Value("${fare.base-fare}")
    private double baseFare;

    @Value("${fare.rate-per-km}")
    private double ratePerKm;

    /**
     * Documented fare calculation rule:
     * fare = baseFare + (distanceKm * ratePerKm)
     * Defaults: baseFare = 200, ratePerKm = 50  =>  fare = 200 + (distance * 50)
     */
    public FareEstimateResponse estimateFare(FareEstimateRequest request) {
        BigDecimal fare = calculateFare(request.getDistanceKm());
        return FareEstimateResponse.builder()
                .estimatedFare(fare)
                .build();
    }

    /**
     * Applies the fare formula to a raw distance value. Public (not just
     * used internally) so it can also be called directly wherever a final
     * fare needs recalculating at ride completion, keeping the same rule
     * used for both the initial estimate and the final charge.
     */
    public BigDecimal calculateFare(double distanceKm) {
        // fare = baseFare + (distanceKm * ratePerKm) - the one documented pricing rule.
        double fare = baseFare + (distanceKm * ratePerKm);
        // Round to 2 decimal places (money should never have more precision than cents).
        return BigDecimal.valueOf(fare).setScale(2, RoundingMode.HALF_UP);
    }
}
