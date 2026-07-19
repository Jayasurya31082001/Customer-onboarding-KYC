package com.example.notificationservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.notificationservice.events.AccountCreatedEvent;
import com.example.notificationservice.events.ApplicationRejectedEvent;

@Service
public class NotificationEventIngressService {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventIngressService.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public NotificationEventIngressService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void onAccountCreated(String customerId, String customerEmail, String accountNumber, String sortCode) {
        LOGGER.info("Publishing account-created notification event. customerId={}, accountNumber={}",
            customerId, accountNumber);
        applicationEventPublisher.publishEvent(
                new AccountCreatedEvent(this, customerId, customerEmail, accountNumber, sortCode));
        LOGGER.info("Published account-created notification event. customerId={}", customerId);
    }

    @Transactional
    public void onApplicationRejected(String customerId, String customerEmail, String reason) {
        LOGGER.info("Publishing application-rejected notification event. customerId={}", customerId);
        applicationEventPublisher.publishEvent(
                new ApplicationRejectedEvent(this, customerId, customerEmail, reason));
        LOGGER.info("Published application-rejected notification event. customerId={}", customerId);
    }
}
