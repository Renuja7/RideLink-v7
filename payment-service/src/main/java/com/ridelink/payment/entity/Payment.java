package com.ridelink.payment.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MongoDB document mapping to the "payments" collection - this is the single
 * source of truth for payment records owned exclusively by this service (no
 * other service is allowed to read or write this collection directly, per
 * the assignment's data-ownership rule). Represents one simulated payment
 * made against a ride.
 */
@Document(collection = "payments")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Payment {

    @Id
    private String id; // MongoDB auto-generates a hex ObjectId string when null on save

    // Stable reference to a ride owned by Ride Management Service; we only
    // store the id, never join across services' databases.
    private String rideId;

    private BigDecimal amount;

    private PaymentMethod paymentMethod;

    // Defaults to PENDING at the Java level, but in this simulated
    // implementation PaymentService always overwrites this with SUCCESS
    // (or throws before saving) since there is no real gateway to wait on.
    @Builder.Default
    private PaymentStatus paymentStatus = PaymentStatus.PENDING;

    // Stamped automatically with the current time on first save via Spring
    // Data MongoDB's auditing support (see MongoConfig's @EnableMongoAuditing),
    // the Mongo-native equivalent of the JPA @PrePersist callback this
    // replaced - callers never need to set the payment date themselves.
    @CreatedDate
    private LocalDateTime paymentDate;
}
