package com.ridelink.payment.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error payload returned by this service for any failed request.
 * All four RideLink microservices agree on this {timestamp, status, message,
 * path} shape so that clients (Postman, Swagger, other services) get a
 * consistent error format no matter which service responded. Built by
 * GlobalExceptionHandler for every handled exception type.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String timestamp; // when the error occurred, as an ISO-8601 string
    private int status; // HTTP status code, e.g. 404, 422, 400
    private String message; // human-readable explanation of what went wrong
    private String path; // the request URI that triggered the error
}
