package com.ridelink.payment.controller;

import com.ridelink.payment.dto.FareEstimateRequest;
import com.ridelink.payment.dto.FareEstimateResponse;
import com.ridelink.payment.service.FareService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller that exposes the fare-estimation endpoint.
 * This is called both directly (e.g. from Swagger/Postman during a demo) and
 * server-to-server by the Ride Management Service via OpenFeign when a rider
 * asks for a fare estimate before requesting a ride - the fare formula itself
 * stays owned by this service so it is never duplicated elsewhere.
 */
@RestController
@RequestMapping("/api/fare")
@RequiredArgsConstructor
@Tag(name = "Fare", description = "Fare estimation")
public class FareController {

    private final FareService fareService;

    /**
     * Estimates the fare for a given pickup/destination pair based on distance.
     * Validates the incoming request body (e.g. distance must be positive) before
     * delegating the actual calculation to FareService, which applies the
     * documented fare = baseFare + (distance * ratePerKm) rule.
     */
    @PostMapping("/estimate")
    @Operation(summary = "Estimate the fare for a pickup/destination pair", description = "fare = 200 + (distance * 50)")
    public ResponseEntity<FareEstimateResponse> estimateFare(@Valid @RequestBody FareEstimateRequest request) {
        return ResponseEntity.ok(fareService.estimateFare(request));
    }
}
