package com.example.datasyncservice.repository;

import com.example.datasyncservice.entity.SyncMetadata;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository for reading and updating synchronization state.
 *
 * <p>The {@code lastSuccessfulSync} timestamp is the incremental cursor
 * — only records created/updated after this point are fetched from upstream services.
 */
@Repository
public interface SyncMetadataRepository extends JpaRepository<SyncMetadata, Long> {

    /**
     * Looks up the sync state for a specific microservice.
     *
     * @param serviceName the Spring application name of the upstream service
     * @return the metadata row, or empty if not yet seeded
     */
    Optional<SyncMetadata> findByServiceName(String serviceName);

    /**
     * Finds all services whose last sync is older than the given threshold.
     * Useful for alerting on stale sync states.
     *
     * @param threshold cutoff time
     * @return list of services that have not synced recently
     */
    List<SyncMetadata> findByLastSuccessfulSyncBefore(LocalDateTime threshold);

    /**
     * Atomically marks a service step as RUNNING before processing begins.
     * Prevents duplicate concurrent executions.
     */
    @Modifying
    @Query("""
            UPDATE SyncMetadata s
               SET s.lastJobStatus = 'RUNNING',
                   s.updatedAt     = :now
             WHERE s.serviceName   = :serviceName
            """)
    void markRunning(@Param("serviceName") String serviceName,
                     @Param("now") LocalDateTime now);

    /**
     * Records a successful sync completion — advances the cursor timestamp.
     */
    @Modifying
    @Query("""
            UPDATE SyncMetadata s
               SET s.lastSuccessfulSync    = :syncTime,
                   s.lastJobStatus         = 'COMPLETED',
                   s.lastProcessedRecord   = :lastRecordId,
                   s.totalRecordsLastSync  = :totalRecords,
                   s.failedRecordsLastSync = :failedRecords,
                   s.updatedAt             = :now
             WHERE s.serviceName = :serviceName
            """)
    void markCompleted(@Param("serviceName") String serviceName,
                       @Param("syncTime") LocalDateTime syncTime,
                       @Param("lastRecordId") String lastRecordId,
                       @Param("totalRecords") long totalRecords,
                       @Param("failedRecords") long failedRecords,
                       @Param("now") LocalDateTime now);

    /**
     * Records a failed sync — does NOT advance the cursor, so next run retries from the same point.
     */
    @Modifying
    @Query("""
            UPDATE SyncMetadata s
               SET s.lastJobStatus         = 'FAILED',
                   s.failedRecordsLastSync = :failedRecords,
                   s.updatedAt             = :now
             WHERE s.serviceName = :serviceName
            """)
    void markFailed(@Param("serviceName") String serviceName,
                    @Param("failedRecords") long failedRecords,
                    @Param("now") LocalDateTime now);
}
