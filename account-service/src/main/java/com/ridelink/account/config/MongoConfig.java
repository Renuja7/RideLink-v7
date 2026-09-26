package com.ridelink.account.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

/**
 * Turns on Spring Data MongoDB's auditing support, which is what makes the
 * {@code @CreatedDate}/{@code @LastModifiedDate} fields on {@link com.ridelink.account.entity.User}
 * get populated automatically on save — the MongoDB equivalent of the JPA
 * {@code @PrePersist}/{@code @PreUpdate} lifecycle callbacks this service used
 * to rely on before its persistence layer moved from MySQL to MongoDB.
 */
@Configuration
@EnableMongoAuditing
public class MongoConfig {
}
