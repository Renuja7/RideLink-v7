package com.ridelink.ride.exception;

import com.ridelink.ride.dto.ErrorResponse;
import feign.FeignException;
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
 * Central place that turns every exception thrown anywhere in this service
 * into a consistent JSON ErrorResponse with an appropriate HTTP status code.
 * @RestControllerAdvice makes Spring apply these handlers to every
 * controller in the service automatically, so individual controller methods
 * don't need their own try/catch blocks — this is what gives the API
 * consistent, predictable error responses (a required assignment quality).
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * A requested ride (or other resource) does not exist.
     *
     * @return 404 Not Found with the exception's message.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * A caller tried to move a ride to a status that isn't allowed from its
     * current status (e.g. REQUESTED straight to COMPLETED). This is one of
     * the assignment's required negative-scenario demonstrations.
     *
     * @return 400 Bad Request with the exception's message.
     */
    @ExceptionHandler(InvalidStatusTransitionException.class)
    public ResponseEntity<ErrorResponse> handleInvalidTransition(InvalidStatusTransitionException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Driver assignment was attempted but driver-service reported no
     * available drivers. Modelled as a conflict (409) because the request
     * itself was valid, but the current system state prevents fulfilling
     * it — another required negative scenario.
     *
     * @return 409 Conflict with the exception's message.
     */
    @ExceptionHandler(NoAvailableDriverException.class)
    public ResponseEntity<ErrorResponse> handleNoDriver(NoAvailableDriverException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Catches failures that come back from calling ANOTHER service
     * (driver-service or payment-service) through a Feign client — for
     * example that service returning a 4xx/5xx, timing out, or being
     * unreachable. This translates a downstream failure into a sensible
     * HTTP response for THIS service's own clients, instead of leaking a
     * raw Feign stack trace or always returning 500.
     *
     * @return the downstream service's own status code when Spring can map
     *         it, otherwise 503 Service Unavailable.
     */
    @ExceptionHandler(FeignException.class)
    public ResponseEntity<ErrorResponse> handleFeignException(FeignException ex, HttpServletRequest request) {
        HttpStatus status = HttpStatus.resolve(ex.status());
        if (status == null) {
            // ex.status() can be -1 or some other value Spring doesn't recognise
            // (e.g. a connection failure rather than a real HTTP response), so we
            // fall back to a generic "downstream unavailable" status.
            status = HttpStatus.SERVICE_UNAVAILABLE;
        }
        return build(status, "Downstream service error: " + ex.getMessage(), request);
    }

    /**
     * Bean validation (@Valid / @NotNull / @NotBlank on request DTOs) failed
     * on an incoming request body.
     *
     * @return 400 Bad Request whose message lists every field validation
     *         error, comma-separated.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Catch-all for business-rule violations raised as a plain
     * IllegalArgumentException elsewhere in the codebase.
     *
     * @return 400 Bad Request with the exception's message.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Last-resort fallback for any exception not handled more specifically
     * above, so the client always gets a well-formed ErrorResponse rather
     * than a raw stack trace or an empty response.
     *
     * @return 500 Internal Server Error with the exception's message.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    /**
     * Shared helper that assembles the standard ErrorResponse body (with a
     * timestamp and the request path) for every handler above, so the error
     * shape stays identical no matter which exception triggered it.
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
