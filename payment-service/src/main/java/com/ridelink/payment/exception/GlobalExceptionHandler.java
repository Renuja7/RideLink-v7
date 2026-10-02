package com.ridelink.payment.exception;

import com.ridelink.payment.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Central place that converts exceptions thrown anywhere in this service
 * into the consistent {timestamp, status, message, path} ErrorResponse shape
 * that all four RideLink services agree on. Using @RestControllerAdvice
 * means controllers themselves stay free of try/catch blocks - they just
 * throw, and this class decides the HTTP status and message.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles a lookup for a payment (or receipt) that does not exist,
     * or a receipt requested for a payment that never succeeded.
     * Maps to 404 Not Found.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles the simulated payment-failure scenario (e.g. missing or
     * non-positive amount). Maps to 422 Unprocessable Entity, since the
     * request was well-formed but the payment itself could not be processed -
     * this is one of the assignment's required negative-scenario demos.
     */
    @ExceptionHandler(PaymentFailedException.class)
    public ResponseEntity<ErrorResponse> handlePaymentFailed(PaymentFailedException ex, HttpServletRequest request) {
        return build(HttpStatus.UNPROCESSABLE_ENTITY, ex.getMessage(), request);
    }

    /**
     * Handles bean-validation failures raised when a @Valid request body
     * fails its @NotBlank/@NotNull/@Positive constraints. Collects every
     * field error into one comma-separated message so the caller sees all
     * problems at once, and maps to 400 Bad Request.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Handles any other invalid-input case signalled manually via
     * IllegalArgumentException. Maps to 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Catch-all fallback for any exception not handled more specifically
     * above, so the client never sees a raw stack trace. Maps to
     * 500 Internal Server Error.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    /**
     * Assembles the shared ErrorResponse body (timestamp, status code,
     * message and the request path that failed) and wraps it in a
     * ResponseEntity with the given HTTP status.
     */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .message(message)
                .path(request.getRequestURI())
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
