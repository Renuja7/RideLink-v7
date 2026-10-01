package com.ridelink.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Consistent JSON shape returned for every error response from this service,
 * built by GlobalExceptionHandler. Keeping one shape for all errors (rather
 * than a different structure per exception type) makes the API predictable
 * for clients like Postman/Swagger and satisfies the "consistent error
 * responses" requirement.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String timestamp;
    private int status;
    private String message;
    private String path;
}
