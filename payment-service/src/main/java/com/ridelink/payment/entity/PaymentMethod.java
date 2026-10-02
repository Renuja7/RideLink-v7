package com.ridelink.payment.entity;

/**
 * The way a simulated payment was made. There is no real payment gateway
 * integration in this assignment, so these values are purely descriptive
 * labels chosen by the caller (e.g. via Postman/Swagger) rather than
 * something validated against an actual card processor or wallet provider.
 */
public enum PaymentMethod {
    CASH,
    CARD,
    WALLET
}
