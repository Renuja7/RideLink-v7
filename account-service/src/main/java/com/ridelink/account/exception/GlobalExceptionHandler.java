package com.ridelink.account.exception;

import com.ridelink.account.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralised exception-to-HTTP-response translator for this whole service.
 * Applies to every controller (thanks to @RestControllerAdvice) so that no matter
 * which endpoint fails, the caller always gets back the same consistent
 * {timestamp, status, message, path} ErrorResponse shape — this same pattern is
 * repeated in all four RideLink microservices for a uniform API contract.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * Handles "not found" business errors (e.g. looking up a user id that doesn't exist)
     * and reports them as HTTP 404.
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex, HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /**
     * Handles attempts to create a resource that already exists (e.g. registering with
     * an email already on file) and reports them as HTTP 409 Conflict.
     */
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex, HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /**
     * Handles failed login attempts — wrong email/password combination, or a
     * Spring-Security-level bad-credentials error — and reports them as HTTP 401
     * Unauthorized. Deliberately uses a generic message upstream ("Invalid email or
     * password") so callers cannot tell whether the email or the password was wrong.
     */
    @ExceptionHandler({InvalidCredentialsException.class, BadCredentialsException.class})
    public ResponseEntity<ErrorResponse> handleInvalidCredentials(RuntimeException ex, HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    /**
     * Handles Bean Validation failures on @Valid request bodies (e.g. a blank email or
     * a too-short password) and reports them as HTTP 400, combining every field error
     * message into one comma-separated string so the caller sees all problems at once.
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex, HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(FieldError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /**
     * Catch-all for invalid-argument style errors raised manually in the service layer,
     * reported as HTTP 400 Bad Request.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, ex.getMessage(), request);
    }

    /**
     * Last-resort fallback for any exception not already handled above, reported as
     * HTTP 500 Internal Server Error so unexpected bugs never leak a raw stack trace
     * to the client — they still get the same consistent error JSON shape.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex, HttpServletRequest request) {
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage(), request);
    }

    // Shared helper that assembles the standard ErrorResponse body (timestamp, status
    // code, message, and the request path that failed) used by every handler above.
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
