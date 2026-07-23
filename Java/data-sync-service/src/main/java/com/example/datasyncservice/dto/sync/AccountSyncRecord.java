package com.example.datasyncservice.dto.sync;

import java.time.LocalDateTime;

/** Inbound sync record from account-service {@code /internal/sync}. */
public record AccountSyncRecord(
        String accountId,
        String customerId,
        String accountNumber,
        String sortCode,
        String accountType,
        String status,
        LocalDateTime createdAt
) {}
