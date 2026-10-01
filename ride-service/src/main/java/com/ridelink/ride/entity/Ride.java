package com.ridelink.ride.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * MongoDB document mapped to the "rides" collection — the single source of
 * truth for a ride's data, owned exclusively by ride-service. No other
 * microservice is allowed to read or write this collection directly (each
 * service owns its own persistence boundary); driver-service and
 * payment-service only ever see ride data through REST calls and DTOs, never
 * through this collection.
 */
@Document(collection = "rides")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Ride {

    @Id
    private String id; // MongoDB auto-generates a hex ObjectId string when null on save

    // Stable id of the passenger (owned by account-service). There is no
    // database foreign key to account-service's table, by design.
    private String passengerId;

    // Stable id of the driver (owned by driver-service). Null until a driver
    // is assigned (see RideService#assignDriver).
    private String driverId;

    private String pickupLocation;

    private String destinationLocation;

    // Upfront estimate, set when the ride is created (from payment-service,
    // or the local fallback formula if that call fails).
    private BigDecimal estimatedFare;

    // Set only when the ride is completed; remains null for every other status.
    private BigDecimal finalFare;

    // Stored as its enum name (e.g. "REQUESTED") rather than an ordinal number,
    // so the field stays human-readable and safe if the enum is reordered.
    @Builder.Default
    private RideStatus status = RideStatus.REQUESTED;

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
