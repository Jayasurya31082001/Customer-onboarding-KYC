package com.example.documentservice.kyc.client;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentUploadedIngressRequest(
        UUID documentId,
        UUID customerId,
        LocalDateTime occurredAt,
        String correlationId
) {
}
