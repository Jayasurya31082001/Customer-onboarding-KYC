package com.example.datasyncservice.dto.sync;

import java.time.LocalDateTime;

/** Inbound sync record from risk-service {@code /internal/sync}. */
public record RiskSyncRecord(
        String riskId,
        String customerId,
        String riskScore,
        String riskCategory,
        String decision,
        LocalDateTime assessedAt
) {}
