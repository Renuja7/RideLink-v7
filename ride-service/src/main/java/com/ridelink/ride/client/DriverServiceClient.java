package com.ridelink.ride.client;

import com.ridelink.ride.dto.DriverResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

/**
 * OpenFeign client for synchronous REST calls from ride-service to driver-service.
 * Used to fetch eligible available drivers when assigning a ride.
 *
 * This interface has no implementation in this codebase — Spring Cloud OpenFeign
 * generates one at runtime (enabled by @EnableFeignClients in
 * RideServiceApplication) that turns each method call into an HTTP request to
 * driver-service, at the base URL configured by services.driver-service.url.
 * This is the assignment's "meaningful interservice interaction" (LO2):
 * ride-service never touches driver-service's database directly, it only
 * calls driver-service's public REST API.
 */
@FeignClient(name = "driver-service", url = "${services.driver-service.url}", path = "/api/drivers")
public interface DriverServiceClient {

    /**
     * Calls GET /api/drivers/available on driver-service to retrieve the list
     * of drivers who are currently marked as available for a new ride.
     * ride-service uses this list when assigning a driver to a ride request;
     * an empty list means there is nobody to assign (see
     * RideService#assignDriver, which turns that into a
     * NoAvailableDriverException / 409 Conflict).
     *
     * @return the available drivers known to driver-service right now.
     */
    @GetMapping("/available")
    List<DriverResponse> getAvailableDrivers();
}
