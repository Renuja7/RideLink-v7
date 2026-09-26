package com.ridelink.account.entity;

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
 * MongoDB document representing a RideLink account (passenger, driver or admin), and the
 * only collection this service owns and is allowed to read/write. Other RideLink services
 * must never query this collection directly — they only ever get user data indirectly,
 * for example through the userId/role claims embedded in a JWT — which is what keeps
 * each microservice's persistence boundary independent.
 *
 * The id is a MongoDB ObjectId, represented here as a String: when a new User is saved
 * with a null id, Spring Data MongoDB generates one automatically, the same way JPA's
 * IDENTITY strategy did for the previous Long id — every other service simply carries
 * this id around as an opaque String reference (userId/passengerId/etc.) rather than a
 * numeric one now.
 */
@Document(collection = "users")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {

    @Id
    private String id;

    private String fullName;

    // Enforced at the DB level too (a unique index), as a second line of defence
    // beyond AuthService's existsByEmail check.
    @Indexed(unique = true)
    private String email;

    private String phoneNumber;

    private String password; // Stores only the BCrypt hash, never the plain-text password

    private Role role; // Stored as the enum's name ("ADMIN") by the Mongo driver automatically

    @Builder.Default
    private AccountStatus status = AccountStatus.ACTIVE; // New accounts are usable immediately by default

    @CreatedDate
    private LocalDateTime createdAt; // Set automatically on first save by Spring Data MongoDB auditing

    @LastModifiedDate
    private LocalDateTime updatedAt; // Refreshed automatically on every subsequent save
}
