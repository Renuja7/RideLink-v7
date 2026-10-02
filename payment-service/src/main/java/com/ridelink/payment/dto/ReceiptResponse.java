package com.ridelink.payment.dto;

import com.ridelink.payment.entity.PaymentMethod;
import com.ridelink.payment.entity.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response body for GET /api/receipts/{id}.
 * This is not backed by its own database table - PaymentService builds one
 * of these on the fly from an existing successful Payment record, adding a
 * generated, human-friendly receiptNumber (e.g. "RCPT-00000001").
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReceiptResponse {
    private String paymentId;
    private String rideId;
    private BigDecimal amountPaid;
    private PaymentMethod paymentMethod;
    private PaymentStatus paymentStatus;
    private LocalDateTime paymentDate;
    private String receiptNumber;
}
