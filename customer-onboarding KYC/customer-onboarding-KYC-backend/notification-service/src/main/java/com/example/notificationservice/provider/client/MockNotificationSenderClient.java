package com.example.notificationservice.provider.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "notification-provider.mock-enabled", havingValue = "true")
public class MockNotificationSenderClient implements NotificationSenderClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockNotificationSenderClient.class);

    @Override
    public void send(String customerEmail, String message, String correlationId) {
        LOGGER.info("Mock notification send enabled; skipping external provider call. customerEmail={}, correlationId={}, message={}",
            customerEmail, correlationId, message);
    }
}
