package com.ridelink.ride.dto;

import com.ridelink.ride.entity.RideStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Public, client-facing view of a Ride, returned by every endpoint in
 * RideController. Kept separate from the Ride JPA entity so the API contract
 * (what Swagger/Postman clients see) doesn't automatically change if the
 * database entity's internal shape changes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RideResponse {
    private String id;
    // Cross-service references — just plain numeric ids pointing at
    // records owned by account-service / driver-service, no shared database.
    private String passengerId;
    private String driverId;
    private String pickupLocation;
    private String destinationLocation;
    private BigDecimal estimatedFare;
    // Set only once the ride reaches COMPLETED; null before that.
    private BigDecimal finalFare;
    private RideStatus status;
    private LocalDateTime createdAt;
}
