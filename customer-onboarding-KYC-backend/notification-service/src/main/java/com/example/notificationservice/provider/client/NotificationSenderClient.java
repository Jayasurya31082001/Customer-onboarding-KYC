package com.example.notificationservice.provider.client;

public interface NotificationSenderClient {

    void send(String customerEmail, String message, String correlationId);
}
