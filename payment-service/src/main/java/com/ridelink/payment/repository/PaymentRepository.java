package com.ridelink.payment.repository;

import com.ridelink.payment.entity.Payment;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for Payment documents - the only class in
 * this service allowed to talk to the "payments" collection directly,
 * keeping the database access boundary clean and easy to reason about.
 * Extending MongoRepository gives us save/findById/findAll/etc. for free.
 */
public interface PaymentRepository extends MongoRepository<Payment, String> {
    /**
     * Looks up the payment recorded against a given ride id, if any.
     * Useful for checking whether a ride has already been paid for.
     */
    Optional<Payment> findByRideId(String rideId);
}
