package com.ridelink.ride.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Mirrors the DriverResponse contract exposed by driver-service.
 * Used as the deserialization target for the DriverServiceClient Feign client.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverResponse {
    // driver-service's own driver id — stored on the Ride entity as driverId,
    // with no foreign key, since each service owns its own database.
    private String id;
    private String userId;
    private String availabilityStatus;
    private String currentLocation;
    private String serviceArea;
}
