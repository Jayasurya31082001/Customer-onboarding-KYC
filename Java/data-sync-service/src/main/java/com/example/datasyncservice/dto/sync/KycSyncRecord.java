package com.example.datasyncservice.dto.sync;

import java.time.LocalDateTime;

/** Inbound sync record from kyc-service {@code /internal/sync}. */
public record KycSyncRecord(
        String kycId,
        String customerId,
        String overallStatus,
        String riskLevel,
        LocalDateTime completedAt,
        LocalDateTime updatedAt
) {}
