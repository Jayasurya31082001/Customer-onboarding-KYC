package com.example.datasyncservice.dto.sync;

import java.time.LocalDateTime;

/** Inbound sync record from notification-service {@code /internal/sync}. */
public record NotificationSyncRecord(
        String notificationId,
        String customerId,
        String channel,
        String status,
        String eventType,
        LocalDateTime sentAt
) {}
