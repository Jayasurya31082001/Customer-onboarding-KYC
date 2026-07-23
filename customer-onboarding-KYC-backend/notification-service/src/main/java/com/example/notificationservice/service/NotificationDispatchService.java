package com.example.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.notificationservice.model.NotificationChannel;
import com.example.notificationservice.model.NotificationLog;
import com.example.notificationservice.model.Template;
import com.example.notificationservice.provider.client.NotificationSenderClient;
import com.example.notificationservice.repository.NotificationLogRepository;

@Service
public class NotificationDispatchService implements NotificationDispatchOperations {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationDispatchService.class);

    private final NotificationTemplateService notificationTemplateService;
    private final NotificationSenderClient notificationSenderClient;
    private final NotificationLogRepository notificationLogRepository;

    public NotificationDispatchService(NotificationTemplateService notificationTemplateService,
                                       NotificationSenderClient notificationSenderClient,
                                       NotificationLogRepository notificationLogRepository) {
        this.notificationTemplateService = notificationTemplateService;
        this.notificationSenderClient = notificationSenderClient;
        this.notificationLogRepository = notificationLogRepository;
    }

    @Transactional
    @Override
    public void sendAccountCreated(String customerId, String customerEmail, String accountNumber, String sortCode,
                                   String correlationId) {
        LOGGER.info("Starting account-created notification dispatch. customerId={}, correlationId={}",
                customerId, correlationId);

        Template template = notificationTemplateService.getActiveTemplate("ACCOUNT_CREATED");
        String message = template.getContent()
                .replace("${customerId}", customerId)
                .replace("${accountNumber}", accountNumber)
                .replace("${sortCode}", sortCode);

        try {
            notificationSenderClient.send(customerEmail, message, correlationId);
            notificationLogRepository.save(buildNotificationLog(customerId, template.getTemplateKey(), message, "SENT"));

            LOGGER.info("Account-created notification delivered successfully. customerId={}, accountNumber={}",
                    customerId, accountNumber);
        } catch (RuntimeException exception) {
            notificationLogRepository.save(buildNotificationLog(customerId, template.getTemplateKey(), message, "FAILED"));
            LOGGER.error("Account-created notification delivery failed. customerId={}, correlationId={}",
                    customerId, correlationId, exception);
            throw exception;
        }
    }

    @Transactional
    @Override
    public void sendApplicationRejected(String customerId, String customerEmail, String reason, String correlationId) {
        LOGGER.info("Starting application-rejected notification dispatch. customerId={}, correlationId={}",
                customerId, correlationId);

        Template template = notificationTemplateService.getActiveTemplate("APPLICATION_REJECTED");
        String message = template.getContent()
                .replace("${customerId}", customerId)
                .replace("${reason}", reason);

        try {
            notificationSenderClient.send(customerEmail, message, correlationId);
            notificationLogRepository.save(buildNotificationLog(customerId, template.getTemplateKey(), message, "SENT"));

            LOGGER.info("Application-rejected notification delivered successfully. customerId={}", customerId);
        } catch (RuntimeException exception) {
            notificationLogRepository.save(buildNotificationLog(customerId, template.getTemplateKey(), message, "FAILED"));
            LOGGER.error("Application-rejected notification delivery failed. customerId={}, correlationId={}",
                    customerId, correlationId, exception);
            throw exception;
        }
    }

    private NotificationLog buildNotificationLog(String customerId, String templateKey, String message, String status) {
        return NotificationLog.builder()
                .customerId(customerId)
                .templateKey(templateKey)
                .channel(NotificationChannel.EMAIL)
                .message(message)
                .status(status)
                .build();
    }
}
