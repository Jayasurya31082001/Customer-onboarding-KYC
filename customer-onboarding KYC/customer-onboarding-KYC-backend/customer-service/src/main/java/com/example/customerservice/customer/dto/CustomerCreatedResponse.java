package com.example.customerservice.customer.dto;

import com.example.customerservice.customer.model.OnboardingStatus;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerCreatedResponse(
        @Schema(description = "Generated customer identifier",
                example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
                format = "uuid")
        UUID customerId,

        @Schema(description = "Current onboarding status", example = "PENDING")
        OnboardingStatus status,

        @Schema(description = "Timestamp when the customer record was created",
                example = "2026-07-09T22:45:21")
        LocalDateTime createdAt
) {
}

