package com.example.customerservice.customer.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.customerservice.customer.model.OnboardingStatus;

import io.swagger.v3.oas.annotations.media.Schema;

public record CustomerResponse(

        @Schema(description = "Unique customer identifier", example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd", format = "uuid")
        UUID customerId,

        @Schema(description = "Customer first name", example = "Jane")
        String firstName,

        @Schema(description = "Customer last name", example = "Doe")
        String lastName,

        @Schema(description = "Customer email address", example = "jane.doe@example.com")
        String email,

        @Schema(description = "Customer date of birth", example = "1990-06-15")
        LocalDate dateOfBirth,

        @Schema(description = "Customer phone number", example = "+447911123456")
        String phoneNumber,

        @Schema(description = "ISO 3166-1 alpha-2 nationality code", example = "GB")
        String nationality,

        @Schema(description = "First line of customer address", example = "10 Downing Street")
        String addressLine1,

        @Schema(description = "City", example = "London")
        String city,

        @Schema(description = "Postcode", example = "SW1A 2AA")
        String postcode,

        @Schema(description = "Current onboarding status", example = "PENDING")
        OnboardingStatus status,

        @Schema(description = "Timestamp when the customer record was created", example = "2026-07-09T22:45:21")
        LocalDateTime createdAt
) {
}
