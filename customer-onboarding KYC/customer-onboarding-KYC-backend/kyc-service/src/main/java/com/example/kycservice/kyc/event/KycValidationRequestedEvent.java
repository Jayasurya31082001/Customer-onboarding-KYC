package com.example.kycservice.kyc.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record KycValidationRequestedEvent(
        UUID kycCaseId,
        UUID customerId,
        UUID documentId,
        LocalDateTime occurredAt,
        String correlationId
) {
}
