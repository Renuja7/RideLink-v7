package com.ridelink.driver.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI/Swagger documentation for this service.
 * The assignment brief requires Swagger UI to be the official interface for
 * developing, testing and demonstrating the API, so this bean gives the
 * generated docs a proper title/description and wires up the "Authorize"
 * button in Swagger UI so a Bearer JWT can be attached to test requests.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Builds the OpenAPI definition used by Swagger UI.
     * Registers a single HTTP Bearer security scheme named "bearerAuth" and
     * applies it globally, so every documented endpoint shows a padlock and
     * accepts a JWT for authenticated/role-restricted testing.
     */
    @Bean
    public OpenAPI driverServiceOpenAPI() {
        final String schemeName = "bearerAuth"; // name referenced by the security requirement below
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Driver & Vehicle Service API")
                        .description("Driver operational profile, vehicle details and availability")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName)) // apply bearer auth to all endpoints in the docs
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
