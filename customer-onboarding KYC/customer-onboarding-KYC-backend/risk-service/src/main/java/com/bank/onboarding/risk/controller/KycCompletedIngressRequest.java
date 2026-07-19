package com.bank.onboarding.risk.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.bank.onboarding.risk.model.KycStatus;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;

public record KycCompletedIngressRequest(
        @NotNull(message = "kycCaseId is required")
        UUID kycCaseId,

        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotNull(message = "documentId is required")
        UUID documentId,

        @NotNull(message = "status is required")
        KycStatus status,

        @NotNull(message = "occurredAt is required")
        LocalDateTime occurredAt,

        String correlationId,

        String nationality,

        LocalDate dateOfBirth,

        boolean pepMatch,

        boolean sanctionsMatch,

        @Email(message = "customerEmail should be a valid email")
        String customerEmail
) {
}