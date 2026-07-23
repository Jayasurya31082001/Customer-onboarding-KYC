package com.example.kycservice.kyc.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.kycservice.customer.client.CustomerClient;
import com.example.kycservice.notification.client.NotificationClient;

@Component
public class ApplicationRejectedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ApplicationRejectedEventListener.class);
    private static final String REJECTED = "REJECTED";

    private final CustomerClient customerClient;
    private final NotificationClient notificationClient;

    public ApplicationRejectedEventListener(CustomerClient customerClient,
                                            NotificationClient notificationClient) {
        this.customerClient = customerClient;
        this.notificationClient = notificationClient;
    }

    @Async("kycTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public void onApplicationRejected(ApplicationRejectedEvent event) {
        customerClient.updateOnboardingStatus(event.customerId(), REJECTED, event.correlationId());

        if (event.customerEmail() == null || event.customerEmail().isBlank()) {
            LOGGER.warn("Skipping application-rejected notification because customer email is missing. customerId={}, correlationId={}",
                    event.customerId(), event.correlationId());
            return;
        }

        notificationClient.sendApplicationRejected(
                event.customerId(),
                event.customerEmail(),
                event.reason(),
                event.correlationId());

        LOGGER.info("Forwarded application-rejected event to notification-service. customerId={}, correlationId={}",
                event.customerId(), event.correlationId());
    }

    @Recover
    public void recover(Exception exception, ApplicationRejectedEvent event) {
        LOGGER.error("Failed to process ApplicationRejectedEvent after retries. customerId={}, correlationId={}",
                event.customerId(), event.correlationId(), exception);
    }
}

