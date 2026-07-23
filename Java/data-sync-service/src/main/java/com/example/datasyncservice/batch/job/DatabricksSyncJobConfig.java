package com.example.datasyncservice.batch.job;

import com.example.datasyncservice.batch.listener.SyncJobExecutionListener;
import com.example.datasyncservice.batch.listener.SyncStepExecutionListener;
import com.example.datasyncservice.batch.reader.RestApiItemReader;
import com.example.datasyncservice.client.*;
import com.example.datasyncservice.dto.databricks.DatabricksRecord;
import com.example.datasyncservice.dto.sync.*;
import com.example.datasyncservice.processor.DataTransformProcessor;
import com.example.datasyncservice.repository.SyncMetadataRepository;
import com.example.datasyncservice.writer.DatabricksItemWriter;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.List;

/**
 * Spring Batch Job and Step configuration for the Databricks sync pipeline.
 *
 * <p>The job runs 6 sequential steps — one per business microservice. Sequential
 * ordering ensures that if customer data hasn't synced yet, downstream analytics
 * that JOIN on customer_id won't reference missing rows.
 *
 * <p>Step naming convention: {@code sync-{service-name}}, e.g. {@code sync-customer-service}.
 * The {@link SyncStepExecutionListener} uses this convention to look up the correct
 * row in {@code sync_metadata}.
 *
 * <p><b>Fault isolation:</b> Each step is independent. A failed {@code sync-risk-service}
 * step fails only that step — {@code sync-customer-service} and {@code sync-document-service}
 * (already completed) are not rolled back. The job itself reports FAILED but partial data
 * is already in Databricks.
 */
@Configuration
public class DatabricksSyncJobConfig {

    @Value("${datasync.batch.chunk-size:1000}")
    private int chunkSize;

    @Value("${datasync.batch.page-size:1000}")
    private int pageSize;

    @Value("${datasync.batch.skip-limit:10}")
    private int skipLimit;

    // ─── Job ──────────────────────────────────────────────────────────────────

    @Bean
    public Job databricksSyncJob(
            JobRepository jobRepository,
            SyncJobExecutionListener jobListener,
            Step syncCustomerStep,
            Step syncDocumentStep,
            Step syncKycStep,
            Step syncRiskStep,
            Step syncAccountStep,
            Step syncNotificationStep) {

        return new JobBuilder("databricks-sync-job", jobRepository)
                .listener(jobListener)
                .start(syncCustomerStep)
                .next(syncDocumentStep)
                .next(syncKycStep)
                .next(syncRiskStep)
                .next(syncAccountStep)
                .next(syncNotificationStep)
                .build();
    }

    // ─── Steps ────────────────────────────────────────────────────────────────

    @Bean
    public Step syncCustomerStep(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            CustomerServiceClient client,
            SyncMetadataRepository metadataRepo,
            DataTransformProcessor processor,
            DatabricksItemWriter writer,
            SyncStepExecutionListener stepListener) {

        return buildStep("sync-customer-service", jobRepository, txManager, stepListener,
                new RestApiItemReader<>(
                        (syncTime, page) -> safeContent(client.fetchPage(syncTime, page, pageSize)),
                        metadataRepo, "customer-service", pageSize),
                processor, writer);
    }

    @Bean
    public Step syncDocumentStep(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            DocumentServiceClient client,
            SyncMetadataRepository metadataRepo,
            DataTransformProcessor processor,
            DatabricksItemWriter writer,
            SyncStepExecutionListener stepListener) {

        return buildStep("sync-document-service", jobRepository, txManager, stepListener,
                new RestApiItemReader<>(
                        (syncTime, page) -> safeContent(client.fetchPage(syncTime, page, pageSize)),
                        metadataRepo, "document-service", pageSize),
                processor, writer);
    }

    @Bean
    public Step syncKycStep(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            KycServiceClient client,
            SyncMetadataRepository metadataRepo,
            DataTransformProcessor processor,
            DatabricksItemWriter writer,
            SyncStepExecutionListener stepListener) {

        return buildStep("sync-kyc-service", jobRepository, txManager, stepListener,
                new RestApiItemReader<>(
                        (syncTime, page) -> safeContent(client.fetchPage(syncTime, page, pageSize)),
                        metadataRepo, "kyc-service", pageSize),
                processor, writer);
    }

    @Bean
    public Step syncRiskStep(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            RiskServiceClient client,
            SyncMetadataRepository metadataRepo,
            DataTransformProcessor processor,
            DatabricksItemWriter writer,
            SyncStepExecutionListener stepListener) {

        return buildStep("sync-risk-service", jobRepository, txManager, stepListener,
                new RestApiItemReader<>(
                        (syncTime, page) -> safeContent(client.fetchPage(syncTime, page, pageSize)),
                        metadataRepo, "risk-service", pageSize),
                processor, writer);
    }

    @Bean
    public Step syncAccountStep(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            AccountServiceClient client,
            SyncMetadataRepository metadataRepo,
            DataTransformProcessor processor,
            DatabricksItemWriter writer,
            SyncStepExecutionListener stepListener) {

        return buildStep("sync-account-service", jobRepository, txManager, stepListener,
                new RestApiItemReader<>(
                        (syncTime, page) -> safeContent(client.fetchPage(syncTime, page, pageSize)),
                        metadataRepo, "account-service", pageSize),
                processor, writer);
    }

    @Bean
    public Step syncNotificationStep(
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            NotificationServiceClient client,
            SyncMetadataRepository metadataRepo,
            DataTransformProcessor processor,
            DatabricksItemWriter writer,
            SyncStepExecutionListener stepListener) {

        return buildStep("sync-notification-service", jobRepository, txManager, stepListener,
                new RestApiItemReader<>(
                        (syncTime, page) -> safeContent(client.fetchPage(syncTime, page, pageSize)),
                        metadataRepo, "notification-service", pageSize),
                processor, writer);
    }

    // ─── Generic step builder ─────────────────────────────────────────────────

    private <T> Step buildStep(
            String stepName,
            JobRepository jobRepository,
            PlatformTransactionManager txManager,
            SyncStepExecutionListener listener,
            RestApiItemReader<T> reader,
            DataTransformProcessor processor,
            DatabricksItemWriter writer) {

        return new StepBuilder(stepName, jobRepository)
                .<T, DatabricksRecord>chunk(chunkSize, txManager)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .faultTolerant()
                .skipLimit(skipLimit)
                .skip(Exception.class)              // Skip individual bad records up to limit
                .noSkip(RuntimeException.class)     // But never skip full write failures
                .listener(listener)
                .build();
    }

    /** Null-safe content extraction from a paginated response. */
    private <T> List<T> safeContent(PagedSyncResponse<T> response) {
        return response == null ? List.of() : response.content();
    }
}
