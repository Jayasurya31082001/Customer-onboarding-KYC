package com.example.datasyncservice.scheduler;

import com.example.datasyncservice.exception.SyncJobException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Triggers the {@code databricks-sync-job} on a configurable cron schedule.
 *
 * <p>Also exposes a manual trigger endpoint at {@code POST /admin/sync/trigger}
 * for on-demand syncs (e.g., after a backfill or data correction).
 *
 * <p><b>Concurrency guard:</b> {@link AtomicBoolean} {@code running} prevents
 * a second scheduler tick from launching a new job while the previous one is
 * still executing. Spring Batch's own {@code JobRepository} also prevents
 * duplicate concurrent jobs, but the guard provides a fast early-exit.
 *
 * <p>The scheduler is disabled when {@code datasync.scheduler.enabled=false},
 * which is useful in local dev or test environments.
 */
@RestController
@RequestMapping("/admin/sync")
@ConditionalOnProperty(name = "datasync.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class DataSyncScheduler {

    private static final Logger log = LoggerFactory.getLogger(DataSyncScheduler.class);

    private final JobLauncher jobLauncher;
    private final Job databricksSyncJob;

    /** Guards against overlapping executions when a job runs longer than the cron interval. */
    private final AtomicBoolean running = new AtomicBoolean(false);

    public DataSyncScheduler(JobLauncher jobLauncher, Job databricksSyncJob) {
        this.jobLauncher      = jobLauncher;
        this.databricksSyncJob = databricksSyncJob;
    }

    /**
     * Scheduled sync trigger. Cron expression is fully configurable via:
     * <ul>
     *   <li>{@code datasync.scheduler.cron} in application.yml</li>
     *   <li>{@code DATASYNC_SCHEDULER_CRON} environment variable (Docker / K8s)</li>
     * </ul>
     * Default: every 15 minutes.
     */
    @Scheduled(cron = "${datasync.scheduler.cron:0 */15 * * * *}")
    public void scheduledSync() {
        log.info("Scheduled sync triggered at {}", LocalDateTime.now());
        launchJob("scheduled");
    }

    /**
     * Manual on-demand trigger. Useful for backfills or post-incident re-syncs.
     * Secured at the network level (admin port 8088 not exposed externally).
     *
     * <p>Example: {@code curl -X POST http://localhost:8087/admin/sync/trigger}
     */
    @PostMapping("/trigger")
    public ResponseEntity<String> manualTrigger() {
        log.info("Manual sync trigger received at {}", LocalDateTime.now());
        boolean launched = launchJob("manual");
        return launched
                ? ResponseEntity.ok("Sync job launched successfully")
                : ResponseEntity.status(409).body("Sync job already running — try again shortly");
    }

    // ─── Private ──────────────────────────────────────────────────────────────

    private boolean launchJob(String triggerSource) {
        if (!running.compareAndSet(false, true)) {
            log.warn("Sync job is already running — skipping {} trigger", triggerSource);
            return false;
        }

        try {
            // Each execution must have unique parameters (timestamp) so Spring Batch
            // doesn't consider it a duplicate of a prior completed run.
            JobParameters params = new JobParametersBuilder()
                    .addString("triggerSource", triggerSource)
                    .addLocalDateTime("triggeredAt", LocalDateTime.now())
                    .toJobParameters();

            jobLauncher.run(databricksSyncJob, params);
            return true;

        } catch (Exception ex) {
            log.error("Failed to launch databricks-sync-job (trigger={}): {}", triggerSource, ex.getMessage(), ex);
            throw new SyncJobException("Failed to launch sync job", ex);
        } finally {
            running.set(false);
        }
    }
}
