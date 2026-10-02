package com.ridelink.payment.dto;

import com.ridelink.payment.entity.PaymentMethod;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Request body for POST /api/payments.
 * rideId is a stable identifier referencing a ride owned by the Ride
 * Management Service - this service never queries that service's database
 * directly, it just stores the id it was given. The @Positive check on
 * amount is what PaymentService's failure simulation relies on: a missing
 * or non-positive amount is treated as a failed payment (422 response).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentRequest {

    @NotNull(message = "rideId is required")
    private String rideId;

    @NotNull(message = "amount is required")
    @Positive(message = "amount must be positive")
    private BigDecimal amount;

    @NotNull(message = "paymentMethod is required")
    private PaymentMethod paymentMethod;
}
