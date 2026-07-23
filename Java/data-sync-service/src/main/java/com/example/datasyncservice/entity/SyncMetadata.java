package com.example.datasyncservice.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * JPA entity that persists per-service synchronization state.
 *
 * <p>One row per upstream microservice. The {@code lastSuccessfulSync} timestamp
 * is the cursor used by {@link com.example.datasyncservice.batch.reader.RestApiItemReader}
 * when calling each service's {@code /internal/sync} endpoint.
 *
 * <p>Updated atomically by {@link com.example.datasyncservice.batch.listener.SyncStepExecutionListener}
 * at the end of every Step execution.
 */
@Entity
@Table(name = "sync_metadata")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SyncMetadata {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    /**
     * Logical name matching the Spring application name of the upstream service.
     * E.g. "customer-service", "account-service".
     * Must be unique — one row per service.
     */
    @Column(name = "service_name", nullable = false, unique = true, length = 100)
    private String serviceName;

    /**
     * UTC timestamp of the last successful sync for this service.
     * Used as the {@code lastSyncTime} query parameter on the next run.
     */
    @Column(name = "last_successful_sync")
    private LocalDateTime lastSuccessfulSync;

    /**
     * Terminal status of the most recent job step for this service.
     * Values: NONE, RUNNING, COMPLETED, FAILED, SKIPPED
     */
    @Column(name = "last_job_status", length = 20)
    private String lastJobStatus;

    /**
     * UUID of the last record successfully pushed to Databricks.
     * Used for debugging and gap analysis — not a resume cursor.
     */
    @Column(name = "last_processed_record", length = 36)
    private String lastProcessedRecord;

    /** Total records pushed to Databricks in the most recent successful sync. */
    @Builder.Default
    @Column(name = "total_records_last_sync", nullable = false)
    private Long totalRecordsLastSync = 0L;

    /** Number of records skipped/failed during the most recent sync. */
    @Builder.Default
    @Column(name = "failed_records_last_sync", nullable = false)
    private Long failedRecordsLastSync = 0L;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
