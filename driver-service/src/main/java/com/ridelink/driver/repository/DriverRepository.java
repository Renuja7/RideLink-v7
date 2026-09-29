package com.ridelink.driver.repository;

import com.ridelink.driver.entity.AvailabilityStatus;
import com.ridelink.driver.entity.Driver;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;

/**
 * Spring Data MongoDB repository for {@link Driver}.
 * Provides the persistence access this service needs without any manual
 * queries - method names are parsed by Spring Data into queries automatically.
 */
public interface DriverRepository extends MongoRepository<Driver, String> {
    // Used to enforce "one driver profile per account" and to look a driver up by their account reference.
    Optional<Driver> findByUserId(String userId);
    // Backs GET /api/drivers/available - the endpoint Ride Service calls to find drivers to assign.
    List<Driver> findByAvailabilityStatus(AvailabilityStatus status);
}
