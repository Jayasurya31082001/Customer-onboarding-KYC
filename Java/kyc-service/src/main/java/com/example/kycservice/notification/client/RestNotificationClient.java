package com.example.kycservice.notification.client;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class RestNotificationClient implements NotificationClient {

    private final RestClient.Builder restClientBuilder;
    private final String notificationServiceBaseUrl;

    public RestNotificationClient(RestClient.Builder restClientBuilder,
                                  @Value("${notification.service.base-url}") String notificationServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    @Override
    public void sendApplicationRejected(UUID customerId, String customerEmail, String reason, String correlationId) {
        RestClient client = restClientBuilder.baseUrl(notificationServiceBaseUrl).build();

        RestClient.RequestBodySpec requestSpec = client.post()
                .uri("/api/internal/events/application-rejected")
                .contentType(MediaType.APPLICATION_JSON);

        if (correlationId != null && !correlationId.isBlank()) {
            requestSpec = requestSpec.header("X-Correlation-ID", correlationId);
        }

        requestSpec
                .body(new ApplicationRejectedIngressRequest(
                        customerId.toString(),
                        customerEmail,
                        reason
                ))
                .retrieve()
                .toBodilessEntity();
    }

    private record ApplicationRejectedIngressRequest(
            String customerId,
            String customerEmail,
            String reason
    ) {
    }
}

