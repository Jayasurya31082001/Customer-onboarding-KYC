package com.example.kycservice.config;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfiguration {

    @Bean
    public OpenAPI kycServiceOpenApi() {
        return new OpenAPI()
                .info(new Info()
                        .title("KYC Service API")
                        .description("OpenAPI documentation for internal event-driven KYC endpoints")
                        .version("v1")
                        .contact(new Contact()
                                .name("Customer Onboarding Team")
                                .email("customer-onboarding@example.com"))
                        .license(new License()
                                .name("Internal Use")
                                .url("https://example.com/internal-use")))
                .servers(List.of(new Server()
                        .url("http://localhost:8083")
                        .description("Local development server")));
    }
}
