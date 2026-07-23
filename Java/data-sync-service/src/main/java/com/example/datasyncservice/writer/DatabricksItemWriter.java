package com.example.datasyncservice.writer;

import com.example.datasyncservice.client.DatabricksClient;
import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Spring Batch {@link ItemWriter} that sends a chunk of {@link DatabricksRecord}s
 * to Databricks in a single batched API call.
 *
 * <p><b>Grouping by record type:</b> Each chunk may contain records from a single
 * service step. Records are grouped by {@code recordType} so each Databricks table
 * receives its own targeted upsert call. This keeps Delta table writes isolated
 * and allows per-table retry granularity.
 *
 * <p><b>Metrics:</b> Increments per-service success/failure counters on every write.
 *
 * <p><b>Idempotency:</b> The {@code checksum} on each record ensures that if the
 * same chunk is re-sent (e.g., after a job restart), Databricks MERGE INTO will
 * silently no-op on existing records.
 */
@Component
public class DatabricksItemWriter implements ItemWriter<DatabricksRecord> {

    private static final Logger log = LoggerFactory.getLogger(DatabricksItemWriter.class);

    private final DatabricksClient databricksClient;
    private final MeterRegistry meterRegistry;

    @Value("${datasync.batch.chunk-size:1000}")
    private int chunkSize;

    public DatabricksItemWriter(DatabricksClient databricksClient,
                                MeterRegistry meterRegistry) {
        this.databricksClient = databricksClient;
        this.meterRegistry    = meterRegistry;
    }

    @Override
    public void write(Chunk<? extends DatabricksRecord> chunk) {
        List<? extends DatabricksRecord> records = chunk.getItems();
        if (records.isEmpty()) {
            return;
        }

        // Group by record type → one Databricks table call per type
        Map<String, List<DatabricksRecord>> byType = records.stream()
                .collect(Collectors.groupingBy(DatabricksRecord::recordType,
                        Collectors.mapping(r -> (DatabricksRecord) r, Collectors.toList())));

        for (Map.Entry<String, List<DatabricksRecord>> entry : byType.entrySet()) {
            String recordType = entry.getKey();
            List<DatabricksRecord> batch = entry.getValue();

            log.info("Writing {} {} records to Databricks", batch.size(), recordType);

            try {
                databricksClient.upsertRecords(batch, recordType);

                // Increment success counter
                successCounter(recordType).increment(batch.size());
                log.info("Successfully wrote {} {} records", batch.size(), recordType);

            } catch (Exception ex) {
                // Increment failure counter — exception will propagate and fail the Step
                failureCounter(recordType).increment(batch.size());
                log.error("Failed to write {} {} records to Databricks: {}",
                        batch.size(), recordType, ex.getMessage(), ex);
                throw ex;  // Re-throw so Spring Batch marks the Step as FAILED
            }
        }
    }

    private Counter successCounter(String recordType) {
        return meterRegistry.counter("data.sync.records.processed",
                "service", recordType.toLowerCase(), "status", "success");
    }

    private Counter failureCounter(String recordType) {
        return meterRegistry.counter("data.sync.records.failed",
                "service", recordType.toLowerCase(), "status", "failed");
    }
}
