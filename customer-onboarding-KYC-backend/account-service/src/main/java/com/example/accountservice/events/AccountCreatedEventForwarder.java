package com.example.accountservice.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.example.accountservice.customer.client.CustomerServiceClient;
import com.example.accountservice.notification.client.AccountCreatedIngressRequest;
import com.example.accountservice.notification.client.NotificationEventPublisherClient;

@Component
public class AccountCreatedEventForwarder {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountCreatedEventForwarder.class);

    private final NotificationEventPublisherClient notificationEventPublisherClient;
    private final CustomerServiceClient customerServiceClient;

    public AccountCreatedEventForwarder(NotificationEventPublisherClient notificationEventPublisherClient,
                                        CustomerServiceClient customerServiceClient) {
        this.notificationEventPublisherClient = notificationEventPublisherClient;
        this.customerServiceClient = customerServiceClient;
    }

    @Async("accountTaskExecutor")
    @EventListener
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public void onAccountCreated(AccountCreatedEvent event) {
        // Always persist APPROVED status first so customer state does not remain KYC_COMPLETED
        // when downstream notification publishing is delayed or fails.
        customerServiceClient.updateOnboardingStatusToApproved(event.getCustomerId());

        notificationEventPublisherClient.publishAccountCreated(new AccountCreatedIngressRequest(
            event.getCustomerId(),
            event.getCustomerEmail(),
            event.getAccountNumber(),
            event.getSortCode()
        ));

        LOGGER.info("Forwarded account-created event to notification-service and updated customer status to APPROVED. customerId={}, accountNumber={}",
                event.getCustomerId(), event.getAccountNumber());
    }

    @Recover
    public void recover(Exception exception, AccountCreatedEvent event) {
        LOGGER.error("Failed to forward account-created event after retries. customerId={}, accountNumber={}",
                event.getCustomerId(), event.getAccountNumber(), exception);
    }
}
