package com.ridelink.ride;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Entry point for the Ride Management Service ("ride-service").
 *
 * This is Member 3's microservice: it owns ride requests, driver assignment
 * and the ride status lifecycle (REQUESTED -> ASSIGNED -> ACCEPTED ->
 * IN_PROGRESS -> COMPLETED, with CANCELLED possible before COMPLETED).
 * It talks to driver-service (to find an available driver) and
 * payment-service (to estimate fares and record simulated payments) over
 * REST using OpenFeign declarative HTTP clients — this is the required
 * interservice-communication piece of the assignment (LO2).
 */
@SpringBootApplication
// Turns on Spring Cloud OpenFeign and tells it where to find the @FeignClient
// interfaces (DriverServiceClient, PaymentServiceClient) so it can generate
// working HTTP client implementations for them at startup.
@EnableFeignClients(basePackages = "com.ridelink.ride.client")
public class RideServiceApplication {
    /**
     * Boots the Spring application context for this service (starts the
     * embedded web server, wires up all beans, opens the database
     * connection, etc.).
     *
     * @param args standard command-line arguments, forwarded to Spring Boot.
     */
    public static void main(String[] args) {
        SpringApplication.run(RideServiceApplication.class, args);
    }
}
