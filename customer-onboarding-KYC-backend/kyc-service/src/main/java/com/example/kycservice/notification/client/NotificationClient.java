package com.example.kycservice.notification.client;

import java.util.UUID;

public interface NotificationClient {

    void sendApplicationRejected(UUID customerId, String customerEmail, String reason, String correlationId);
}

