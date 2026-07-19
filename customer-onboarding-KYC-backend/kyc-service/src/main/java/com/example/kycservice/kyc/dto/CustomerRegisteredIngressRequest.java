package com.example.kycservice.kyc.dto;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.validation.constraints.NotNull;

public record CustomerRegisteredIngressRequest(
        @NotNull(message = "customerId is required")
        UUID customerId,

        @NotNull(message = "occurredAt is required")
        LocalDateTime occurredAt,

        String correlationId
) {
}
