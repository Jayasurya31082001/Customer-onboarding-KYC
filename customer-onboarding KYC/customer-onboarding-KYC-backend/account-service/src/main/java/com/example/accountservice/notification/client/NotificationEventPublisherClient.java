package com.example.accountservice.notification.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class NotificationEventPublisherClient {

    private final RestClient.Builder restClientBuilder;
    private final String notificationServiceBaseUrl;

    public NotificationEventPublisherClient(RestClient.Builder restClientBuilder,
                                            @Value("${notification.service.base-url}") String notificationServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.notificationServiceBaseUrl = notificationServiceBaseUrl;
    }

    public void publishAccountCreated(AccountCreatedIngressRequest request) {
        RestClient client = restClientBuilder.baseUrl(notificationServiceBaseUrl).build();
        client.post()
                .uri("/api/internal/events/account-created")
                .body(request)
                .retrieve()
                .toBodilessEntity();
    }
}
