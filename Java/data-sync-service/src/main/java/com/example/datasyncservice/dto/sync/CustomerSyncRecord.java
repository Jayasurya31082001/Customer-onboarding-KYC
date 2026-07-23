package com.example.datasyncservice.dto.sync;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Inbound sync record from customer-service {@code /internal/sync}.
 *
 * <p>Contains only the fields required for analytics — no PII beyond what
 * Databricks needs for the KYC onboarding dashboard.
 *
 * <p>Business microservice note: expose this shape from the customer-service
 * internal endpoint. No Databricks knowledge is required in customer-service.
 */
public record CustomerSyncRecord(

        /** UUID primary key from customer-service. */
        String customerId,

        String firstName,
        String lastName,

        /** Hashed/masked in production before syncing. */
        String email,

        /** ISO-8601 date string, e.g. "1990-05-22". */
        LocalDate dateOfBirth,

        /** ISO 3166-1 alpha-2 country code, e.g. "GB". */
        String nationality,

        String city,
        String postcode,

        /** One of: PENDING, KYC_IN_PROGRESS, KYC_COMPLETED, APPROVED, MANUAL_APPROVAL_REQUIRED, REJECTED */
        String status,

        /** When the customer row was first inserted. */
        LocalDateTime createdAt,

        /** When the customer row was last updated (the sync cursor field). */
        LocalDateTime updatedAt
) {}
