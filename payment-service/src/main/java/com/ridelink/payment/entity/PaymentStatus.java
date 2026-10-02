package com.ridelink.payment.entity;

/**
 * Lifecycle status of a payment. This simulated implementation only ever
 * writes SUCCESS to the database (a failed attempt throws an exception
 * before a row is saved, rather than being persisted as FAILED). PENDING and
 * FAILED are kept in the model anyway so a future real payment-gateway
 * integration (which would need to wait asynchronously for a result) could
 * use this same enum without any schema change.
 */
public enum PaymentStatus {
    PENDING,
    SUCCESS,
    FAILED
}
