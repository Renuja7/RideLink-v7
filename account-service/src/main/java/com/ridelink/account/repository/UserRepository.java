package com.ridelink.account.repository;

import com.ridelink.account.entity.User;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

/**
 * Spring Data MongoDB repository for the User document — the sole gateway this service
 * uses to read/write the "users" collection. Extending MongoRepository gives standard
 * CRUD methods (save, findById, findAll, ...) for free, and Spring generates the
 * implementation of the custom finder methods below automatically from their names,
 * exactly as JpaRepository did before this service moved from MySQL to MongoDB.
 */
public interface UserRepository extends MongoRepository<User, String> {
    // Looks a user up by email; used at login time to find who is trying to authenticate.
    Optional<User> findByEmail(String email);

    // Cheap existence check used during registration to reject duplicate email addresses
    // without loading the whole User document.
    boolean existsByEmail(String email);
}
