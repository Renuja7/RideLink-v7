package com.ridelink.ride.client;

import com.ridelink.ride.dto.CreatePaymentRequest;
import com.ridelink.ride.dto.FareEstimateRequest;
import com.ridelink.ride.dto.FareEstimateResponse;
import com.ridelink.ride.dto.PaymentResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

/**
 * OpenFeign client for synchronous REST calls from ride-service to payment-service.
 * Used for fare estimation and to trigger simulated payment creation on ride completion.
 *
 * Like DriverServiceClient, this is a declarative HTTP client: Spring Cloud
 * OpenFeign implements it automatically, so calling a method here really
 * means "make an HTTP request to payment-service and wait for the response".
 * Fare & Payment Service owns its own fare-calculation rule and payment data;
 * ride-service only ever reaches it through this REST contract, never through
 * a shared database.
 */
@FeignClient(name = "payment-service", url = "${services.payment-service.url}", path = "/api")
public interface PaymentServiceClient {

    /**
     * Calls POST /api/fare/estimate on payment-service to get a fare estimate
     * for a given pickup/destination/distance. Used when a ride is first
     * created, so the passenger can see an upfront estimate. If this call
     * fails, RideService#createRide falls back to computing the same
     * documented formula locally rather than blocking ride creation.
     *
     * @param request pickup location, destination location and distance in km.
     * @return the estimated fare for that trip.
     */
    @PostMapping("/fare/estimate")
    FareEstimateResponse getFareEstimate(@RequestBody FareEstimateRequest request);

    /**
     * Calls POST /api/payments on payment-service to record a simulated
     * payment once a ride completes. Used by RideService#completeRide;
     * failures here are intentionally tolerated (see the try/catch there)
     * because payment recording can be retried later without blocking the
     * fact that the ride itself has finished.
     *
     * @param request the ride id, final amount and payment method to record.
     * @return the created payment record, including its status.
     */
    @PostMapping("/payments")
    PaymentResponse createPayment(@RequestBody CreatePaymentRequest request);
}
