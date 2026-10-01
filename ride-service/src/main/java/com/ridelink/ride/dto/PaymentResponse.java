package com.ridelink.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response returned by payment-service after a simulated payment is created
 * for a completed ride. ride-service only reads this for logging/awareness —
 * a failure to receive it does not block ride completion (see
 * RideService#completeRide, which tolerates payment-call failures).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentResponse {
    private String id;
    private String rideId;
    private BigDecimal amount;
    private String paymentMethod;
    private String paymentStatus;
}
