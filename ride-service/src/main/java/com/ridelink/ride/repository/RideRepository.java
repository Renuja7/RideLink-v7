package com.ridelink.ride.repository;

import com.ridelink.ride.entity.Ride;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;

/**
 * Spring Data MongoDB repository for the Ride document. Extending
 * MongoRepository gives us CRUD and basic query methods (save, findById,
 * etc.) for free; this is the only place in the codebase that talks to the
 * "rides" collection directly, keeping the database access isolated behind
 * RideService.
 */
public interface RideRepository extends MongoRepository<Ride, String> {
    // Method name alone tells Spring Data MongoDB how to build the query
    // — no manual query needed. Useful for a passenger to see their own
    // ride history.
    List<Ride> findByPassengerId(String passengerId);

    // Same pattern, filtered by driver instead — useful for a driver to see
    // rides assigned to them.
    List<Ride> findByDriverId(String driverId);
}
