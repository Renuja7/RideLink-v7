package com.ridelink.payment.service;

import com.ridelink.payment.dto.PaymentRequest;
import com.ridelink.payment.dto.PaymentResponse;
import com.ridelink.payment.entity.Payment;
import com.ridelink.payment.entity.PaymentMethod;
import com.ridelink.payment.entity.PaymentStatus;
import com.ridelink.payment.exception.PaymentFailedException;
import com.ridelink.payment.exception.ResourceNotFoundException;
import com.ridelink.payment.repository.PaymentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PaymentService, using Mockito to fake the repository so no
 * real database is needed. Covers both the happy path (a valid payment
 * succeeds and a receipt can be issued) and the required negative
 * scenarios (invalid amount fails, missing payment cannot get a receipt).
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @InjectMocks
    private PaymentService paymentService;

    private Payment payment;

    @BeforeEach
    void setUp() {
        payment = Payment.builder()
                .id("1")
                .rideId("50")
                .amount(BigDecimal.valueOf(700))
                .paymentMethod(PaymentMethod.CASH)
                .paymentStatus(PaymentStatus.SUCCESS)
                .paymentDate(LocalDateTime.now())
                .build();
    }

    @Test
    void createPayment_shouldSucceed_whenAmountIsValid() {
        PaymentRequest request = new PaymentRequest("50", BigDecimal.valueOf(700), PaymentMethod.CASH);

        when(paymentRepository.save(any(Payment.class))).thenReturn(payment);

        PaymentResponse response = paymentService.createPayment(request);

        assertEquals(PaymentStatus.SUCCESS, response.getPaymentStatus());
        assertEquals(BigDecimal.valueOf(700), response.getAmount());
    }

    @Test
    void createPayment_shouldThrow_whenAmountIsZeroOrNegative() {
        PaymentRequest request = new PaymentRequest("50", BigDecimal.ZERO, PaymentMethod.CASH);

        assertThrows(PaymentFailedException.class, () -> paymentService.createPayment(request));
        verify(paymentRepository, never()).save(any(Payment.class));
    }

    @Test
    void getReceiptById_shouldThrow_whenPaymentNotFound() {
        when(paymentRepository.findById("99")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> paymentService.getReceiptById("99"));
    }

    @Test
    void getReceiptById_shouldReturnReceipt_whenPaymentSuccessful() {
        when(paymentRepository.findById("1")).thenReturn(Optional.of(payment));

        var receipt = paymentService.getReceiptById("1");

        assertEquals("RCPT-1", receipt.getReceiptNumber());
        assertEquals("50", receipt.getRideId());
    }
}
