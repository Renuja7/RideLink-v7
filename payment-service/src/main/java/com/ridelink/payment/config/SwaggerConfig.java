package com.ridelink.payment.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI/Swagger documentation for this service.
 * This is what powers the Swagger UI page, which (per the assignment brief)
 * is one of the official interfaces used to demonstrate and test the API -
 * no separate frontend is required.
 */
@Configuration
public class SwaggerConfig {

    /**
     * Builds the OpenAPI definition shown in Swagger UI: sets the API title,
     * description and version, and declares a "bearerAuth" JWT security scheme
     * so Swagger UI can send an Authorization: Bearer <token> header when
     * calling protected endpoints.
     */
    @Bean
    public OpenAPI paymentServiceOpenAPI() {
        final String schemeName = "bearerAuth"; // name referenced by the security requirement and scheme below
        return new OpenAPI()
                .info(new Info()
                        .title("RideLink - Fare & Payment Service API")
                        .description("Fare estimation, simulated payment processing and receipts")
                        .version("1.0.0"))
                .addSecurityItem(new SecurityRequirement().addList(schemeName))
                .components(new Components()
                        .addSecuritySchemes(schemeName, new SecurityScheme()
                                .name(schemeName)
                                .type(SecurityScheme.Type.HTTP)
                                .scheme("bearer")
                                .bearerFormat("JWT")));
    }
}
