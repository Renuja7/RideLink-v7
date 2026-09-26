package com.ridelink.account;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Entry point for the Account Service microservice.
 * This service is the "Member 1" service in the RideLink system: it owns passenger
 * and driver account registration, login/JWT issuance, role management and
 * account-status management. It is started and run completely independently of
 * the other three services (Driver & Vehicle, Ride Management, Fare & Payment).
 */
@SpringBootApplication
public class AccountServiceApplication {
    // Boots the embedded server, wires up Spring beans (controllers, services,
    // repositories, security config) and starts listening for HTTP requests.
    public static void main(String[] args) {
        SpringApplication.run(AccountServiceApplication.class, args);
    }
}
