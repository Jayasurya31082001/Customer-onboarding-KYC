package com.example.datasyncservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.retry.annotation.EnableRetry;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Entry point for the data-sync-service.
 *
 * <p>This service is responsible for:
 * <ul>
 *   <li>Pulling incremental changes from all 6 business microservices via /internal/sync endpoints.</li>
 *   <li>Transforming domain records into a unified Databricks schema.</li>
 *   <li>Writing batched records to Databricks using Spring Batch chunk processing.</li>
 *   <li>Tracking sync state in the sync_metadata table.</li>
 * </ul>
 *
 * <p><b>Note:</b> {@code spring.batch.job.enabled=false} in application.yml intentionally
 * prevents automatic job execution on startup — the scheduler ({@link com.example.datasyncservice.scheduler.DataSyncScheduler})
 * is the sole driver of job execution.
 */
@SpringBootApplication
@EnableScheduling
@EnableRetry
public class DataSyncServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(DataSyncServiceApplication.class, args);
    }
}
