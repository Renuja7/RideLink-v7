package com.ridelink.driver;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Driver & Vehicle Service (Member 2's microservice).
 * This service owns driver operational profiles, vehicle details, availability
 * status, simulated live location and declared service area. It runs as its
 * own independently deployable Spring Boot application with its own database,
 * separate from Account, Ride and Fare & Payment services.
 */
@SpringBootApplication
public class DriverServiceApplication {
    /**
     * Boots the Spring application context for this microservice.
     * Nothing beyond the standard Spring Boot bootstrap is needed here -
     * all real configuration lives in the config/security/service classes.
     */
    public static void main(String[] args) {
        SpringApplication.run(DriverServiceApplication.class, args);
    }
}
