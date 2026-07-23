package com.example.datasyncservice.client;

import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.exception.DatabricksWriteException;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTP client for the Databricks REST API.
 *
 * <p>Submits batches of {@link DatabricksRecord} to a Databricks SQL warehouse
 * via the REST API. Each batch targets a specific Delta table and uses
 * MERGE semantics (driven by the {@code checksum} field) for idempotency.
 *
 * <p><b>Retry</b>: {@code @Retryable} handles transient 5xx / 429 responses
 * with exponential backoff (configured in application.yml). After all attempts
 * are exhausted, {@link #recover} throws a {@link DatabricksWriteException}
 * which fails the Spring Batch step.
 */
@Component
public class DatabricksClient {

    private static final Logger log = LoggerFactory.getLogger(DatabricksClient.class);

    private final RestClient restClient;
    private final MeterRegistry meterRegistry;

    @Value("${databricks.catalog:kyc_analytics}")
    private String catalog;

    @Value("${databricks.schema:onboarding}")
    private String schema;

    public DatabricksClient(
            @Qualifier("databricksRestClient") RestClient restClient,
            MeterRegistry meterRegistry) {
        this.restClient    = restClient;
        this.meterRegistry = meterRegistry;
    }

    /**
     * Writes a batch of records to the Databricks Delta table for the given record type.
     *
     * <p>Table name is derived as: {@code <catalog>.<schema>.<recordType_lowercase>_delta}
     * e.g., {@code kyc_analytics.onboarding.customer_delta}
     *
     * @param records       the batch of records to push
     * @param recordType    e.g. "CUSTOMER", "ACCOUNT"
     */
    @Retryable(
            retryFor  = { HttpServerErrorException.class, RestClientException.class },
            maxAttemptsExpression = "${datasync.retry.databricks.max-attempts:5}",
            backoff = @Backoff(
                    delayExpression    = "${datasync.retry.databricks.initial-interval-ms:1000}",
                    multiplierExpression = "${datasync.retry.databricks.multiplier:2.0}",
                    maxDelayExpression = "${datasync.retry.databricks.max-interval-ms:30000}"
            )
    )
    public void upsertRecords(List<DatabricksRecord> records, String recordType) {
        String tableName = buildTableName(recordType);
        log.debug("Pushing {} records to Databricks table: {}", records.size(), tableName);

        Timer.Sample sample = Timer.start(meterRegistry);
        try {
            Map<String, Object> requestBody = buildRequestBody(records, tableName);

            restClient.post()
                    .uri("/api/2.0/sql/statements")
                    .body(requestBody)
                    .retrieve()
                    .toBodilessEntity();

            sample.stop(meterRegistry.timer("data.sync.databricks.write.latency",
                    "table", tableName, "status", "success"));

            log.info("Successfully pushed {} records to {}", records.size(), tableName);

        } catch (Exception ex) {
            sample.stop(meterRegistry.timer("data.sync.databricks.write.latency",
                    "table", tableName, "status", "error"));
            log.warn("Databricks write attempt failed for table {} — will retry. Cause: {}",
                    tableName, ex.getMessage());
            throw ex;
        }
    }

    /**
     * Recovery method invoked by Spring Retry after all retries are exhausted.
     * Wraps the exception in {@link DatabricksWriteException} to fail the Batch Step cleanly.
     */
    @Recover
    public void recover(Exception ex, List<DatabricksRecord> records, String recordType) {
        String tableName = buildTableName(recordType);
        meterRegistry.counter("data.sync.records.failed",
                "service", recordType.toLowerCase(),
                "reason", "databricks_write_failure").increment(records.size());

        log.error("All Databricks retry attempts exhausted for table {}. {} records lost. Cause: {}",
                tableName, records.size(), ex.getMessage(), ex);

        throw new DatabricksWriteException(
                "Failed to write %d records to %s after all retries".formatted(records.size(), tableName), ex);
    }

    /**
     * Performs a lightweight health check against the Databricks workspace.
     * Used by the custom {@code DatabricksHealthIndicator}.
     */
    public boolean isHealthy() {
        try {
            restClient.get()
                    .uri("/api/2.0/clusters/list")
                    .retrieve()
                    .toBodilessEntity();
            return true;
        } catch (Exception ex) {
            log.warn("Databricks health check failed: {}", ex.getMessage());
            return false;
        }
    }

    // ─── Private helpers ──────────────────────────────────────────────────────

    private String buildTableName(String recordType) {
        return "%s.%s.%s_delta".formatted(catalog, schema, recordType.toLowerCase());
    }

    /**
     * Builds the Databricks SQL API request body that performs a MERGE INTO
     * using the checksum as the deduplication key.
     *
     * <p>In production, replace with Databricks JDBC or Delta Live Tables
     * for higher throughput. The SQL API is used here for simplicity
     * and to avoid extra SDK dependencies.
     */
    private Map<String, Object> buildRequestBody(List<DatabricksRecord> records, String tableName) {
        // Build a VALUES clause from the records
        StringBuilder values = new StringBuilder();
        for (int i = 0; i < records.size(); i++) {
            DatabricksRecord r = records.get(i);
            if (i > 0) values.append(",");
            values.append("('%s','%s','%s','%s','%s','%s','%s')"
                    .formatted(
                            escape(r.sourceService()),
                            escape(r.recordType()),
                            escape(r.recordId()),
                            escape(r.customerId()),
                            escape(r.payload().toString()),
                            r.eventTime() != null ? r.eventTime().toString() : "NULL",
                            escape(r.checksum())
                    ));
        }

        String sql = """
                MERGE INTO %s AS target
                USING (
                  SELECT col1 AS source_service, col2 AS record_type, col3 AS record_id,
                         col4 AS customer_id, col5 AS payload, col6 AS event_time, col7 AS checksum
                  FROM VALUES %s
                ) AS source
                ON target.checksum = source.checksum
                WHEN NOT MATCHED THEN INSERT *
                WHEN MATCHED THEN UPDATE SET target.synced_at = current_timestamp()
                """.formatted(tableName, values);

        Map<String, Object> body = new HashMap<>();
        body.put("statement", sql);
        body.put("wait_timeout", "30s");
        body.put("on_wait_timeout", "CANCEL");
        return body;
    }

    private String escape(String value) {
        return value == null ? "" : value.replace("'", "''");
    }
}
