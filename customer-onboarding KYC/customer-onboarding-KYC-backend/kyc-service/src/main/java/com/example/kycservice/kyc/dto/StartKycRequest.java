package com.example.kycservice.kyc.dto;

import java.time.LocalDate;
import java.util.UUID;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;

@Builder
public record StartKycRequest(
        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotNull(message = "documentId is required")
        UUID documentId,

        @NotBlank(message = "firstName is required")
        String firstName,

        @NotBlank(message = "lastName is required")
        String lastName,

        @NotNull(message = "dateOfBirth is required")
        LocalDate dateOfBirth
) {
}
