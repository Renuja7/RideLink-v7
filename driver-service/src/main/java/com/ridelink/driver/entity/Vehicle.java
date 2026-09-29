package com.ridelink.driver.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

/**
 * MongoDB document representing a registered vehicle.
 * Kept separate from {@link Driver} on purpose: vehicle details (plate
 * number, model, type) rarely change, whereas a driver's operational state
 * changes constantly. This separation follows the Single Responsibility
 * Principle and would allow one driver to own several vehicles in future
 * without touching the Driver entity's shape.
 */
@Document(collection = "vehicles")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Vehicle {

    @Id
    private String id; // MongoDB auto-generates a hex ObjectId string when null on save

    // References a Driver document's id within THIS service's own database, so
    // (unlike Driver.userId) this can safely be treated as an internal id.
    private String driverId;

    @Indexed(unique = true) // prevents the same plate/number being registered twice
    private String vehicleNumber;

    private String vehicleModel;

    private VehicleType vehicleType;
}
