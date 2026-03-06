package com.medicalstore.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * SpringDoc OpenAPI configuration — defines the Swagger UI metadata,
 * server list, and the cookie-based security scheme used by the
 * MedicalStore application.
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI medicalStoreOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("MedicalStore API")
                        .description("REST API for MedicalStore — Pharmacy Inventory & Sales Management System. "
                                + "Supports medicines, sales, customers, suppliers, analytics, and dashboard KPIs.")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("MedicalStore Team")
                                .url("https://github.com/Kt129673/medicalstore"))
                        .license(new License()
                                .name("MIT License")
                                .url("https://opensource.org/licenses/MIT")))
                .servers(List.of(
                        new Server().url("http://localhost:8081").description("Local Development")))
                .components(new Components()
                        .addSecuritySchemes("cookieAuth",
                                new SecurityScheme()
                                        .type(SecurityScheme.Type.APIKEY)
                                        .in(SecurityScheme.In.COOKIE)
                                        .name("JSESSIONID")
                                        .description("Login via /login form to obtain session cookie")))
                .addSecurityItem(new SecurityRequirement().addList("cookieAuth"));
    }
}
