package com.example.kycservice.kyc.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import com.example.kycservice.kyc.model.KycStatus;

public record KycCompletedEvent(
        UUID kycCaseId,
        UUID customerId,
        UUID documentId,
        KycStatus status,
        LocalDateTime occurredAt,
        String correlationId,
        String nationality,
        LocalDate dateOfBirth,
        boolean pepMatch,
        boolean sanctionsMatch,
        String customerEmail
) {
}
