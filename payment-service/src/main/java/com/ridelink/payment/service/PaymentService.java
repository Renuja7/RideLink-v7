package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.entity.Payment;
import com.ridelink.payment.entity.PaymentStatus;
import com.ridelink.payment.exception.PaymentFailedException;
import com.ridelink.payment.exception.ResourceNotFoundException;
import com.ridelink.payment.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * Core business logic for recording simulated payments and issuing
 * receipts. This is the class that implements the "record a simulated
 * payment" and "receipt generation and retrieval" responsibilities called
 * for by the assignment brief, and is called both directly by clients and
 * server-to-server by the Ride Management Service via OpenFeign when a ride
 * completes.
 */
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;

    /**
     * Records a simulated payment for a ride. There is no real payment
     * gateway to call, so "processing" a payment here just means validating
     * the amount and saving a record with SUCCESS status. An amount that is
     * missing or not strictly positive is treated as a failed payment and
     * throws PaymentFailedException, which GlobalExceptionHandler maps to a
     * 422 response - this is the required "failed simulated payment"
     * negative-scenario demo.
     */
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        // Business rule: an amount of null or <= 0 counts as a failed payment attempt.
        if (request.getAmount() == null || request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new PaymentFailedException("Simulated payment failed: invalid amount");
        }

        Payment payment = Payment.builder()
                .rideId(request.getRideId())
                .amount(request.getAmount())
                .paymentMethod(request.getPaymentMethod())
                // Simulated gateway: we go straight to SUCCESS since there is no
                // real asynchronous processor to wait on (see PaymentStatus for why
                // PENDING/FAILED still exist in the model).
                .paymentStatus(PaymentStatus.SUCCESS)
                .build();

        Payment saved = paymentRepository.save(payment);
        return toResponse(saved);
    }

    /**
     * Fetches a single payment by id, converted to its API response shape.
     * Throws ResourceNotFoundException (mapped to 404) if no such payment exists.
     */
    public PaymentResponse getPaymentById(String id) {
        return toResponse(findPaymentOrThrow(id));
    }

    /**
     * Builds a receipt for a completed payment. A receipt is not stored as
     * its own entity - it is derived on demand from the underlying Payment
     * record, with a generated receipt number. Only payments that actually
     * succeeded have anything to issue a receipt for; requesting one for a
     * payment that is PENDING or FAILED (or that doesn't exist) results in
     * a 404, since there is nothing valid to show.
     */
    public ReceiptResponse getReceiptById(String paymentId) {
        Payment payment = findPaymentOrThrow(paymentId);

        // No receipt exists for a payment that never succeeded.
        if (payment.getPaymentStatus() != PaymentStatus.SUCCESS) {
            throw new ResourceNotFoundException("Receipt unavailable: payment " + paymentId + " was not successful");
        }

        return ReceiptResponse.builder()
                .paymentId(payment.getId())
                .rideId(payment.getRideId())
                .amountPaid(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                // Human-friendly receipt number derived from the payment's MongoDB
                // ObjectId, e.g. "RCPT-671e2f9a1b2c3d4e5f678901". Uppercased so it
                // reads like a conventional receipt/reference code.
                .receiptNumber("RCPT-" + payment.getId().toUpperCase())
                .build();
    }

    /**
     * Shared lookup helper: fetches a Payment by id or throws
     * ResourceNotFoundException (-> 404) if it doesn't exist, so both
     * getPaymentById and getReceiptById fail the same consistent way.
     */
    private Payment findPaymentOrThrow(String id) {
        return paymentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Payment not found with id: " + id));
    }

    /** Maps the Payment entity to its public PaymentResponse DTO. */
    private PaymentResponse toResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .rideId(payment.getRideId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .paymentStatus(payment.getPaymentStatus())
                .paymentDate(payment.getPaymentDate())
                .build();
    }
}
