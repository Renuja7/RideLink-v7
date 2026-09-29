package com.ridelink.driver.exception;

import com.ridelink.driver.dto.ErrorResponse;
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
 * Centralised error handling for the whole service.
 * Converts every exception type thrown by controllers/services into the same
 * consistent {@link ErrorResponse} JSON shape ({timestamp, status, message,
 * path}) - the same pattern used across all four RideLink services, so
 * Postman/Swagger consumers get predictable error handling regardless of
 * which service they call.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Maps a missing driver/vehicle lookup to HTTP 404 Not Found. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** Maps an attempt to create an already-existing resource to HTTP 409 Conflict. */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Maps Bean Validation failures (e.g. @NotNull/@NotBlank on a request DTO)
     * to HTTP 400 Bad Request, joining all field error messages into one
     * readable string instead of leaking Spring's default validation payload.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /** Maps a manually-thrown IllegalArgumentException to HTTP 400 Bad Request. */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /** Catch-all fallback: any unexpected exception becomes HTTP 500 Internal Server Error. */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    /** Shared helper that assembles the consistent ErrorResponse body for every handler above. */
    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message, HttpServletRequest request) {
        ErrorResponse error = ErrorResponse.builder()
                .timestamp(LocalDateTime.now().toString())
                .status(status.value())
                .message(message)
                .path(request.getRequestURI()) // which endpoint the failing request hit
                .build();
        return ResponseEntity.status(status).body(error);
    }
}
