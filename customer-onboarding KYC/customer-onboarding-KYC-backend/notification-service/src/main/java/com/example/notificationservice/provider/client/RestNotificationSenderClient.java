package com.example.notificationservice.provider.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.example.notificationservice.exception.NotificationDeliveryException;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
@ConditionalOnProperty(name = "notification-provider.mock-enabled", havingValue = "false", matchIfMissing = true)
public class RestNotificationSenderClient implements NotificationSenderClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(RestNotificationSenderClient.class);

    private final RestClient.Builder restClientBuilder;
    private final String notificationProviderBaseUrl;

    public RestNotificationSenderClient(RestClient.Builder restClientBuilder,
                                        @Value("${notification-provider.base-url}") String notificationProviderBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.notificationProviderBaseUrl = notificationProviderBaseUrl;
    }

    @Override
    @CircuitBreaker(name = "notificationProvider", fallbackMethod = "fallbackSend")
    @Retry(name = "notificationProvider")
    public void send(String customerEmail, String message, String correlationId) {
        LOGGER.info("Sending notification via provider. correlationId={}", correlationId);

        RestClient client = restClientBuilder.baseUrl(notificationProviderBaseUrl).build();
        client.post()
                .uri("/api/v1/notifications/send")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Correlation-ID", correlationId == null ? "" : correlationId)
                .body(new NotificationProviderRequest(customerEmail, message))
                .retrieve()
                .toBodilessEntity();

        LOGGER.info("Notification provider call completed. correlationId={}", correlationId);
    }

    public void fallbackSend(String customerEmail, String message, String correlationId, Exception exception) {
        LOGGER.warn("Notification provider fallback triggered. correlationId={}, reason={}",
            correlationId, exception.getMessage(), exception);
        throw new NotificationDeliveryException("Notification provider unavailable", exception);
    }

    private record NotificationProviderRequest(String customerEmail, String message) {
    }
}
