package com.ridelink.payment.controller;

import com.ridelink.payment.dto.ReceiptResponse;
import com.ridelink.payment.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for retrieving a receipt for a payment.
 * A receipt is not stored as its own database row - it is generated on the
 * fly from an existing successful Payment record, so this controller is a
 * thin wrapper around PaymentService's receipt-building logic.
 */
@RestController
@RequestMapping("/api/receipts")
@RequiredArgsConstructor
@Tag(name = "Receipts", description = "Receipt generation and retrieval")
public class ReceiptController {

    private final PaymentService paymentService;

    /**
     * Retrieves the receipt for a given payment id. Only payments with a
     * SUCCESS status have a receipt to issue; requesting a receipt for any
     * other payment (or a non-existent one) results in a 404 response.
     */
    @GetMapping("/{id}")
    @Operation(summary = "Retrieve the receipt for a successful payment")
    public ResponseEntity<ReceiptResponse> getReceipt(@PathVariable String id) {
        return ResponseEntity.ok(paymentService.getReceiptById(id));
    }
}
