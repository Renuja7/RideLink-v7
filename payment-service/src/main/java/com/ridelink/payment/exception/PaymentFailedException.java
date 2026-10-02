package com.ridelink.payment.exception;

/**
 * Thrown when a simulated payment cannot be processed (e.g. a missing or
 * non-positive amount). Caught by GlobalExceptionHandler and turned into a
 * 422 Unprocessable Entity response - this is what demonstrates the
 * "failed simulated payment" negative scenario required by the assignment.
 */
public class PaymentFailedException extends RuntimeException {
    /** Creates the exception carrying a human-readable failure reason. */
    public PaymentFailedException(String message) {
        super(message);
    }
}
