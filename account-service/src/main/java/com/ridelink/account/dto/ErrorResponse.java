package com.ridelink.account.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error payload returned by GlobalExceptionHandler for any failed request.
 * Every one of the four RideLink services follows this exact same
 * {timestamp, status, message, path} shape, so API consumers (Postman, other services,
 * a future frontend) get a single consistent error format across the whole system.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String timestamp; // When the error occurred (ISO-8601 string)
    private int status;       // HTTP status code, e.g. 404, 401, 400
    private String message;   // Human-readable explanation of what went wrong
    private String path;      // The request URI that caused the error
}
