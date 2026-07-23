package com.example.datasyncservice.batch.listener;

import com.example.datasyncservice.repository.SyncMetadataRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Listens to each Spring Batch Step lifecycle to:
 * <ul>
 *   <li><b>beforeStep</b>: sets MDC context for structured logging, marks service as RUNNING.</li>
 *   <li><b>afterStep</b>: updates {@code sync_metadata} — advances cursor on SUCCESS, records FAILED status otherwise.</li>
 * </ul>
 *
 * <p>The cursor advancement (updating {@code last_successful_sync}) is intentionally
 * done ONLY on COMPLETED status. A FAILED step will retry from the same point on the
 * next scheduled execution.
 */
@Component
public class SyncStepExecutionListener implements StepExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(SyncStepExecutionListener.class);

    private final SyncMetadataRepository metadataRepo;

    public SyncStepExecutionListener(SyncMetadataRepository metadataRepo) {
        this.metadataRepo = metadataRepo;
    }

    @Override
    public void beforeStep(StepExecution stepExecution) {
        String serviceName = extractServiceName(stepExecution.getStepName());
        MDC.put("serviceName", serviceName);
        MDC.put("jobId", stepExecution.getJobExecutionId().toString());

        log.info("Starting sync step [{}] jobId={}", serviceName, stepExecution.getJobExecutionId());
        metadataRepo.markRunning(serviceName, LocalDateTime.now());
    }

    @Override
    @Transactional
    public ExitStatus afterStep(StepExecution stepExecution) {
        String serviceName = extractServiceName(stepExecution.getStepName());
        ExitStatus exitStatus = stepExecution.getExitStatus();

        try {
            if (ExitStatus.COMPLETED.getExitCode().equals(exitStatus.getExitCode())) {
                LocalDateTime syncedAt = LocalDateTime.now();
                long totalRecords = stepExecution.getWriteCount();
                long failedRecords = stepExecution.getSkipCount();

                metadataRepo.markCompleted(
                        serviceName,
                        syncedAt,
                        null,         // lastProcessedRecord — set from writer in future iteration
                        totalRecords,
                        failedRecords,
                        syncedAt
                );

                log.info("Step COMPLETED [{}] — wrote={}, skipped={}, syncedAt={}",
                        serviceName, totalRecords, failedRecords, syncedAt);

            } else {
                long failedRecords = stepExecution.getSkipCount() + stepExecution.getWriteSkipCount();
                metadataRepo.markFailed(serviceName, failedRecords, LocalDateTime.now());

                log.error("Step FAILED [{}] — exitStatus={}, reads={}, writes={}, skips={}",
                        serviceName, exitStatus.getExitCode(),
                        stepExecution.getReadCount(), stepExecution.getWriteCount(),
                        stepExecution.getSkipCount());
            }
        } finally {
            MDC.remove("serviceName");
            MDC.remove("jobId");
        }

        return exitStatus;
    }

    /**
     * Step names follow the convention "sync-{service-name}", e.g. "sync-customer-service".
     * This method extracts the service name from the step name.
     */
    private String extractServiceName(String stepName) {
        return stepName.replace("sync-", "");
    }
}
