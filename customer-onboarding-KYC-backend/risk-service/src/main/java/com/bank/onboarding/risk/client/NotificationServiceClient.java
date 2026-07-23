package com.bank.onboarding.risk.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.bank.onboarding.risk.exception.DownstreamIntegrationException;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class NotificationServiceClient {

    private final RestClient.Builder restClientBuilder;
    private final String notificationServiceBaseUrl;

    public NotificationServiceClient(RestClient.Builder restClientBuilder,
                                     @Value("${notification.service.base-url}") String notificationServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    public void sendApplicationRejected(String customerId, String customerEmail, String reason) {
        try {
            RestClient client = restClientBuilder.baseUrl(notificationServiceBaseUrl).build();
            client.post()
                    .uri("/api/internal/events/application-rejected")
                    .body(new ApplicationRejectedIngressRequest(customerId, customerEmail, reason))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Sent application-rejected notification for customerId={}", customerId);
        } catch (Exception e) {
            log.error("Failed to send application-rejected notification for customerId={}", customerId, e);
            throw new DownstreamIntegrationException("Failed to send application-rejected notification", e);
        }
    }

    public record ApplicationRejectedIngressRequest(String customerId, String customerEmail, String reason) {}
}
