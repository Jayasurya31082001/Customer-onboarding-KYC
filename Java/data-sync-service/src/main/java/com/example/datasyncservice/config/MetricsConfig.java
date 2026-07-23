package com.example.datasyncservice.config;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.binder.MeterBinder;
import com.example.datasyncservice.repository.SyncMetadataRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

/**
 * Registers custom Micrometer metrics for the data-sync-service.
 *
 * <p>The most critical metric is {@code data.sync.last.sync.age.seconds} — a Gauge
 * per service that measures how old the last successful sync is. An alert can
 * fire when this exceeds a threshold (e.g., 1800 seconds = 30 minutes).
 *
 * <p>Counter metrics (records processed/failed/skipped) are registered directly
 * in {@link com.example.datasyncservice.writer.DatabricksItemWriter} and
 * {@link com.example.datasyncservice.batch.listener.SyncStepExecutionListener}.
 */
@Configuration
public class MetricsConfig {

    private static final String[] SERVICE_NAMES = {
            "customer-service", "document-service", "kyc-service",
            "risk-service", "account-service", "notification-service"
    };

    /**
     * Registers a Gauge per service that reports how many seconds have elapsed
     * since the last successful sync. Value of -1 means the service has never synced.
     */
    @Bean
    public MeterBinder syncAgeGauges(SyncMetadataRepository repo) {
        return (MeterRegistry registry) -> {
            for (String serviceName : SERVICE_NAMES) {
                registry.gauge(
                        "data.sync.last.sync.age.seconds",
                        java.util.List.of(io.micrometer.core.instrument.Tag.of("service", serviceName)),
                        repo,
                        r -> r.findByServiceName(serviceName)
                                .map(meta -> {
                                    if (meta.getLastSuccessfulSync() == null) return -1.0;
                                    return (double) ChronoUnit.SECONDS.between(
                                            meta.getLastSuccessfulSync(), LocalDateTime.now());
                                })
                                .orElse(-1.0)
                );
            }
        };
    }
}
