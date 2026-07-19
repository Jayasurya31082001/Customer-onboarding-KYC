package com.example.notificationservice.service;

public interface NotificationDispatchOperations {

    void sendAccountCreated(String customerId, String customerEmail, String accountNumber, String sortCode,
                            String correlationId);

    void sendApplicationRejected(String customerId, String customerEmail, String reason, String correlationId);
}
