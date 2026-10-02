package com.ridelink.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Response body for POST /api/fare/estimate - just the calculated fare
 * amount, as a BigDecimal to avoid floating-point rounding issues with money.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FareEstimateResponse {
    private BigDecimal estimatedFare;
}
