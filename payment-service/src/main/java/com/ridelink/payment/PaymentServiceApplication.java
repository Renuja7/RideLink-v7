package com.ridelink.payment;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Fare & Payment Service (Member 4's microservice).
 * This service owns fare estimation, simulated payment recording and receipt
 * generation, and runs as its own independently deployable Spring Boot process
 * with its own database - no other service reads or writes its tables directly.
 */
@SpringBootApplication
public class PaymentServiceApplication {
    /**
     * Boots the Spring application context, starting the embedded web server
     * and wiring up all controllers, services and repositories in this service.
     */
    public static void main(String[] args) {
        SpringApplication.run(PaymentServiceApplication.class, args);
    }
}
