package com.example.documentservice.document.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentUploadedEvent(
        UUID documentId,
        UUID customerId,
        String fileName,
        String contentType,
        LocalDateTime occurredAt,
        String correlationId
) {
}
