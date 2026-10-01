package com.ridelink.ride.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI/Swagger documentation for this service.
 * This is what makes the interactive Swagger UI page work, and it is one of
 * the assignment's required deliverables (Swagger UI/OpenAPI as the official
 * interface for demonstrating REST endpoints, since no frontend is built).
 */
@Configuration
public class SwaggerConfig {

    /**
     * Builds the OpenAPI description document for ride-service: title,
     * description, version, and a "bearerAuth" security scheme so Swagger UI
     * shows an "Authorize" button that lets a tester paste in a JWT and have
     * it sent with every request, matching this service's JWT-based security.
     *
     * @return the OpenAPI model Springdoc uses to render /swagger-ui and
     *         /v3/api-docs for this service.
     */
    @Bean
    public OpenAPI rideServiceOpenAPI() {
        final String schemeName = "bearerAuth";
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Ride Management Service API")
                        .description("Ride requests, driver assignment and ride lifecycle management")
                        .version("1.0.0"))
                // Declares that endpoints require this security scheme by default,
                // so Swagger UI knows to attach the Authorization header once set.
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        // Describes the scheme itself: a Bearer token in the
                        // Authorization header, formatted as a JWT.
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
