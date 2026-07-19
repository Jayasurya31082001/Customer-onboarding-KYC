package com.example.customerservice.customer.dto;

import com.example.customerservice.customer.model.OnboardingStatus;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record UpdateCustomerStatusRequest(
        @Schema(description = "Target onboarding status", example = "KYC_IN_PROGRESS", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "status is required")
        OnboardingStatus status
) {
}
