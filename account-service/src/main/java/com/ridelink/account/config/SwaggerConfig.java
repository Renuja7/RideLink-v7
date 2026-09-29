package com.ridelink.account.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI/Swagger documentation for this service.
 * This is what powers the Swagger UI page and the exported OpenAPI spec that the
 * assignment brief requires as the "official interface" for demonstrating the API,
 * since no frontend is being built.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Builds the OpenAPI definition shown in Swagger UI, including title/description/version
     * metadata and a reusable "bearerAuth" security scheme so protected endpoints can be
     * tried out directly from Swagger UI by pasting in a JWT token.
     */
    @Bean
    public OpenAPI accountServiceOpenAPI() {
        final String schemeName = "bearerAuth"; // Name referenced by @SecurityRequirement/@Tag usage in Swagger
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Account Service API")
                        .description("Passenger and driver account management, authentication and authorization")
                        .version("1.0.0"))
                // Tells Swagger UI that endpoints require a bearer token by default.
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        // Declares how the bearer token should be sent (HTTP Authorization header,
                        // "Bearer" scheme, JWT format) so Swagger UI renders an "Authorize" button.
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
