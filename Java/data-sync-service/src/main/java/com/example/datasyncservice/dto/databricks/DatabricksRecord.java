package com.example.datasyncservice.dto.databricks;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Canonical record pushed to Databricks.
 *
 * <p>All 6 microservices' data is normalised into this flat shape before
 * being written to the Databricks Delta table. This eliminates schema drift
 * per service and allows a single generic writer.
 *
 * <p>The {@code checksum} field enables idempotent MERGE INTO operations in
 * Databricks — re-sending the same record is safe.
 *
 * <p>Built using the builder pattern (Lombok) to keep constructors readable
 * when transformers compose the record from domain fields.
 *
 * @param sourceService  The Spring application name of the source service (e.g. "customer-service").
 * @param recordType     Domain entity type (e.g. "CUSTOMER", "ACCOUNT", "KYC", "RISK", "DOCUMENT", "NOTIFICATION").
 * @param recordId       Primary key from the source service (UUID string).
 * @param customerId     The customer this record belongs to — present on every type for analytics joins.
 * @param payload        Flat key-value map of all analytics-relevant fields.
 * @param eventTime      The domain event timestamp (e.g. createdAt / updatedAt from the source entity).
 * @param syncedAt       UTC timestamp when data-sync-service produced this record.
 * @param checksum       SHA-256(recordType + recordId + payload) for Databricks MERGE deduplication.
 */
public record DatabricksRecord(
        String sourceService,
        String recordType,
        String recordId,
        String customerId,
        Map<String, Object> payload,
        LocalDateTime eventTime,
        LocalDateTime syncedAt,
        String checksum
) {
    /**
     * Convenience factory that auto-sets {@code syncedAt} to now.
     * Transformers should prefer this over the full constructor.
     */
    public static DatabricksRecord of(
            String sourceService,
            String recordType,
            String recordId,
            String customerId,
            Map<String, Object> payload,
            LocalDateTime eventTime,
            String checksum
    ) {
        return new DatabricksRecord(
                sourceService, recordType, recordId,
                customerId, payload, eventTime,
                LocalDateTime.now(), checksum
        );
    }
}
