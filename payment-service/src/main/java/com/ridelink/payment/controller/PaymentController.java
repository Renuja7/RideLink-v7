package com.ridelink.payment.controller;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller that exposes payment recording and lookup endpoints.
 * No real payment gateway is involved (per the assignment's simulated-context
 * rules) - this simply records a payment attempt against a ride and reports
 * whether it "succeeded". Also invoked server-to-server by Ride Service via
 * OpenFeign when a ride is completed and payment needs to be recorded.
 */
@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Simulated payment recording")
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Records a simulated payment for a completed ride. Returns 201 Created
     * with the saved payment record on success; the underlying service throws
     * PaymentFailedException (mapped to 422) for the "failed simulated payment"
     * negative scenario, e.g. when the amount is missing or not positive.
     */
    @PostMapping
    @Operation(summary = "Record a simulated payment for a completed ride")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(paymentService.createPayment(request));
    }

    /**
     * Retrieves a single payment record by its id. Returns 404 (via
     * ResourceNotFoundException) if no payment exists with that id.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Get a payment record by id")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getPaymentById(id));
    }
}
