package com.example.notificationservice.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.notificationservice.service.NotificationDispatchOperations;

@Component
public class NotificationEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventListener.class);

    private final NotificationDispatchOperations notificationDispatchService;

    public NotificationEventListener(NotificationDispatchOperations notificationDispatchService) {
        this.notificationDispatchService = notificationDispatchService;
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onAccountCreated(AccountCreatedEvent event) {
        LOGGER.info("Received account-created event for notification delivery. customerId={}",
            event.getCustomerId());

        notificationDispatchService.sendAccountCreated(
                event.getCustomerId(),
            event.getCustomerEmail(),
                event.getAccountNumber(),
                event.getSortCode(),
                null
        );

        LOGGER.info("Completed account-created notification flow. customerId={}, accountNumber={}",
                event.getCustomerId(), event.getAccountNumber());
    }

    @Async("notificationTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onApplicationRejected(ApplicationRejectedEvent event) {
        LOGGER.info("Received application-rejected event for notification delivery. customerId={}",
            event.getCustomerId());

        notificationDispatchService.sendApplicationRejected(
                event.getCustomerId(),
                event.getCustomerEmail(),
                event.getReason(),
                null);

        LOGGER.info("Completed application-rejected notification flow. customerId={}", event.getCustomerId());
    }
}
