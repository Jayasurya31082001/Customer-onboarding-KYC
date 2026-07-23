package com.example.datasyncservice.client;

import com.example.datasyncservice.dto.sync.NotificationSyncRecord;
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

@Component
public class NotificationServiceClient {

    private static final Logger log = LoggerFactory.getLogger(NotificationServiceClient.class);
    private static final String SERVICE_NAME = "notification-service";

    private final RestClient restClient;

    public NotificationServiceClient(@Qualifier("notificationRestClient") RestClient restClient) {
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
    public PagedSyncResponse<NotificationSyncRecord> fetchPage(
            LocalDateTime lastSyncTime, int page, int size) {
        return restClient.get()
                .uri(b -> b.path("/internal/sync")
                        .queryParam("lastSyncTime", lastSyncTime.toString())
                        .queryParam("page", page)
                        .queryParam("size", size)
                        .build())
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});
    }

    @Recover
    public PagedSyncResponse<NotificationSyncRecord> recover(
            RestClientException ex, LocalDateTime lastSyncTime, int page, int size) {
        log.error("[{}] All retries exhausted for page={}: {}", SERVICE_NAME, page, ex.getMessage(), ex);
        throw new ServiceFetchException(SERVICE_NAME,
                "Failed to fetch from %s (page=%d)".formatted(SERVICE_NAME, page), ex);
    }
}
