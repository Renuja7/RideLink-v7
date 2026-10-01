package com.ridelink.ride.config;

import feign.Logger;
import feign.RequestInterceptor;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Shared OpenFeign configuration. Feign client scanning itself is enabled via
 * @EnableFeignClients on RideServiceApplication.
 *
 * This class exists mainly to solve one real bug found during development:
 * driver-service and payment-service both require a valid JWT on their
 * protected endpoints (see their own SecurityConfig), but a Feign call made
 * from inside ride-service does NOT automatically carry the original
 * passenger/driver's Authorization header — Feign builds a brand new,
 * anonymous HTTP request. Without the interceptor below, every call from
 * RideService to driver-service or payment-service would be rejected with
 * 403 Forbidden, because as far as those services can tell, the request has
 * no logged-in user at all.
 */
@Configuration
public class FeignConfig {

    /**
     * Turns on basic request/response logging for every Feign client in this
     * service, which is very useful for debugging interservice calls (e.g.
     * seeing exactly what ride-service sent to driver-service) during
     * development and in the viva.
     *
     * @return the logging verbosity Feign should use.
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.BASIC;
    }

    /**
     * Propagates the caller's JWT (Authorization header) from the inbound request
     * that ride-service is handling onto every outbound Feign call it makes to
     * driver-service and payment-service. Without this, downstream services would
     * reject the internal call because their own JwtAuthFilter/SecurityConfig
     * require a valid Bearer token on protected endpoints.
     *
     * @return a Feign RequestInterceptor that Spring Cloud OpenFeign applies to
     *         every outgoing Feign request built by this service.
     */
    @Bean
    public RequestInterceptor authorizationPropagatingInterceptor() {
        // This lambda IS the RequestInterceptor: Feign calls it once per outbound
        // request, right before sending it, passing in the RequestTemplate it is
        // about to send so we can add/modify headers on it.
        return requestTemplate -> {
            // RequestContextHolder gives us access to the HTTP request that is
            // CURRENTLY being handled by this service (e.g. the passenger's
            // "create ride" request) from code that isn't a controller method —
            // this is how we reach back to the original inbound request while
            // we're deep inside a Feign client call.
            ServletRequestAttributes attributes =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attributes != null) {
                // attributes can be null if this Feign call happens outside a normal
                // web request (e.g. a scheduled job) — in that case there is no
                // Authorization header to copy, so we just skip propagation.
                HttpServletRequest request = attributes.getRequest();
                String authorizationHeader = request.getHeader("Authorization");
                if (authorizationHeader != null) {
                    // Copy the exact same "Bearer <jwt>" header onto the outbound
                    // Feign request, so driver-service/payment-service see the same
                    // authenticated identity that called ride-service in the first place.
                    requestTemplate.header("Authorization", authorizationHeader);
                }
            }
        };
    }
}
