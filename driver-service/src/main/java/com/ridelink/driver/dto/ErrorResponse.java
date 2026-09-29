package com.ridelink.driver.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Standard error payload returned by {@link
 * com.ridelink.driver.exception.GlobalExceptionHandler} for every failure
 * case. The same {timestamp, status, message, path} shape is used across all
 * four RideLink services so clients (Swagger/Postman/other services) can
 * handle errors consistently regardless of which service responded.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse {
    private String timestamp; // when the error was produced
    private int status; // HTTP status code, e.g. 404, 409, 400
    private String message; // human-readable explanation of what went wrong
    private String path; // the request URI that triggered the error
}
