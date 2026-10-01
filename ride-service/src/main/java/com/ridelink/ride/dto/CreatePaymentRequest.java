package com.ridelink.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request payload sent to payment-service's /api/payments endpoint
 * once a ride is completed.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CreatePaymentRequest {
    // Stable numeric id of the ride this payment belongs to — payment-service
    // has no direct access to ride-service's database, so this id is the only link.
    private String rideId;
    private BigDecimal amount;
    private String paymentMethod;
}
