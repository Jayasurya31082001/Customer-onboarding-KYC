package com.example.datasyncservice.dto.sync;

import java.time.LocalDateTime;

/** Inbound sync record from document-service {@code /internal/sync}. */
public record DocumentSyncRecord(
        String documentId,
        String customerId,
        String documentType,
        String status,
        LocalDateTime uploadedAt,
        LocalDateTime verifiedAt
) {}
