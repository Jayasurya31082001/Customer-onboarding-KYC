package com.example.datasyncservice.client;

import com.example.datasyncservice.dto.sync.CustomerSyncRecord;
import com.example.datasyncservice.dto.sync.PagedSyncResponse;
import com.example.datasyncservice.exception.ServiceFetchException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.LocalDateTime;

/**
 * REST client for customer-service's {@code GET /internal/sync} endpoint.
 *
 * <p>{@code @Retryable} handles transient network errors with configurable
 * exponential backoff. The {@code @Recover} method converts persistent
 * failures into a {@link ServiceFetchException} that fails the Batch Step.
 */
@Component
public class CustomerServiceClient {

    private static final Logger log = LoggerFactory.getLogger(CustomerServiceClient.class);
    private static final String SERVICE_NAME = "customer-service";

    private final RestClient restClient;

    public CustomerServiceClient(@Qualifier("customerRestClient") RestClient restClient) {
        this.restClient = restClient;
    }

    @Retryable(
            retryFor  = { RestClientException.class },
            maxAttemptsExpression = "${datasync.retry.service-fetch.max-attempts:3}",
            backoff = @Backoff(
                    delayExpression      = "${datasync.retry.service-fetch.initial-interval-ms:500}",
                    multiplierExpression = "${datasync.retry.service-fetch.multiplier:1.5}",
                    maxDelayExpression   = "${datasync.retry.service-fetch.max-interval-ms:10000}"
            )
    )
    public PagedSyncResponse<CustomerSyncRecord> fetchPage(
            LocalDateTime lastSyncTime, int page, int size) {

        log.debug("[{}] Fetching page={}, size={}, since={}", SERVICE_NAME, page, size, lastSyncTime);

        return restClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/internal/sync")
                        .queryParam("lastSyncTime", lastSyncTime.toString())
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Recover
    public PagedSyncResponse<CustomerSyncRecord> recover(
            RestClientException ex, LocalDateTime lastSyncTime, int page, int size) {

        log.error("[{}] All retry attempts exhausted fetching page={} since={}: {}",
                SERVICE_NAME, page, lastSyncTime, ex.getMessage(), ex);
        throw new ServiceFetchException(SERVICE_NAME,
                "Failed to fetch from %s after all retries (page=%d)".formatted(SERVICE_NAME, page), ex);
    }
}
