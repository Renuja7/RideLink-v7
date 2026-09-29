package com.ridelink.driver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

/**
 * MongoDB document representing a driver's operational profile - the data this
 * service owns exclusively (its own persistence boundary, per the brief's
 * "no direct cross-service DB access" rule).
 * Driver is deliberately kept separate from {@link Vehicle}: a driver's
 * availability, location and service area change constantly, while vehicle
 * details rarely change. Splitting them follows the Single Responsibility
 * Principle and leaves room to let one driver register multiple vehicles
 * later without reshaping this entity.
 */
@Document(collection = "drivers")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Driver {

    @Id
    private String id; // MongoDB auto-generates a hex ObjectId string when null on save

    // Stable reference to the account owner in Account Service's own database.
    // This is intentionally NOT a foreign key: Driver Service must never
    // directly query or join against another service's tables.
    // Uniqueness enforces "one driver profile per account".
    @Indexed(unique = true)
    private String userId;

    // Defaults new drivers to UNAVAILABLE so they must explicitly opt in to
    // receiving ride assignments after registering.
    @Builder.Default
    private AvailabilityStatus availabilityStatus = AvailabilityStatus.UNAVAILABLE;

    private String currentLocation; // simulated live position; updated frequently

    private String serviceArea; // broader declared operating zone; updated rarely, more like a preference

    @CreatedDate
    private LocalDateTime createdAt;

    @LastModifiedDate
    private LocalDateTime updatedAt;
}
