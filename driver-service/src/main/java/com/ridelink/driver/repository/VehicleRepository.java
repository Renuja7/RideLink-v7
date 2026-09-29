package com.ridelink.driver.repository;

import com.ridelink.driver.entity.Vehicle;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for {@link Vehicle}.
 * Provides the persistence access this service needs without any manual
 * queries - method names are parsed by Spring Data into queries automatically.
 */
public interface VehicleRepository extends MongoRepository<Vehicle, String> {
    // Looks up all vehicles belonging to a given driver.
    List<Vehicle> findByDriverId(String driverId);
    // Used to reject duplicate vehicle registrations by plate/number.
    Optional<Vehicle> findByVehicleNumber(String vehicleNumber);
}
