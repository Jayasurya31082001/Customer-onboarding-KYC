package com.example.datasyncservice.batch.listener;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.BatchStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDateTime;

/**
 * Listens to the top-level Spring Batch Job lifecycle.
 *
 * <p>Records overall job duration and completion status in Micrometer.
 * Logs a structured summary after every job run.
 */
@Component
public class SyncJobExecutionListener implements JobExecutionListener {

    private static final Logger log = LoggerFactory.getLogger(SyncJobExecutionListener.class);

    private final MeterRegistry meterRegistry;
    private Timer.Sample timerSample;

    public SyncJobExecutionListener(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    @Override
    public void beforeJob(JobExecution jobExecution) {
        timerSample = Timer.start(meterRegistry);
        log.info("=== databricks-sync-job STARTED | jobId={} | params={} ===",
                jobExecution.getJobId(),
                jobExecution.getJobParameters());
    }

    @Override
    public void afterJob(JobExecution jobExecution) {
        BatchStatus status = jobExecution.getStatus();
        long durationMs = Duration.between(
                jobExecution.getStartTime(), jobExecution.getEndTime()).toMillis();

        // Record job duration
        if (timerSample != null) {
            timerSample.stop(meterRegistry.timer("data.sync.job.duration",
                    "job", "databricks-sync-job",
                    "status", status.name()));
        }

        // Increment job completion counter
        meterRegistry.counter("data.sync.job.executions",
                "job", "databricks-sync-job",
                "status", status.name()).increment();

        if (status == BatchStatus.COMPLETED) {
            log.info("=== databricks-sync-job COMPLETED | jobId={} | durationMs={} ===",
                    jobExecution.getJobId(), durationMs);
        } else {
            log.error("=== databricks-sync-job {} | jobId={} | durationMs={} | failures={} ===",
                    status, jobExecution.getJobId(), durationMs,
                    jobExecution.getAllFailureExceptions());
        }
    }
}
