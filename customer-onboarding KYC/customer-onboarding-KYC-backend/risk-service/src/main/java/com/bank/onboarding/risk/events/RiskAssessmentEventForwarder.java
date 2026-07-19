package com.bank.onboarding.risk.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import com.bank.onboarding.risk.client.AccountServiceClient;
import com.bank.onboarding.risk.client.CustomerServiceClient;
import com.bank.onboarding.risk.client.NotificationServiceClient;
import com.bank.onboarding.risk.model.Disposition;

@Component
public class RiskAssessmentEventForwarder {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskAssessmentEventForwarder.class);

    private final AccountServiceClient accountServiceClient;
    private final CustomerServiceClient customerServiceClient;
    private final NotificationServiceClient notificationServiceClient;

    public RiskAssessmentEventForwarder(AccountServiceClient accountServiceClient,
                                       CustomerServiceClient customerServiceClient,
                                       NotificationServiceClient notificationServiceClient) {
        this.accountServiceClient = accountServiceClient;
        this.customerServiceClient = customerServiceClient;
        this.notificationServiceClient = notificationServiceClient;
    }

    @Async("riskTaskExecutor")
    @EventListener
    public void onRiskAssessed(RiskAssessedEvent event) {
        if (event.getDisposition() != Disposition.AUTO_APPROVE) {
            return;
        }

        accountServiceClient.publishRiskAssessed(
            event.getCustomerId(),
            event.getCustomerEmail(),
            event.getDisposition(),
            event.getScore()
        );
        LOGGER.info("Forwarded risk-assessed auto-approve event to account-service. customerId={}, score={}",
            event.getCustomerId(), event.getScore());
    }

    @Async("riskTaskExecutor")
    @EventListener
    public void onManualReviewRequired(ManualReviewRequiredEvent event) {
        customerServiceClient.updateOnboardingStatusToManualReview(event.getCustomerId());
        LOGGER.info("Forwarded manual-review-required event to customer-service. customerId={}",
                event.getCustomerId());
    }

    @Async("riskTaskExecutor")
    @EventListener
    public void onApplicationRejected(ApplicationRejectedEvent event) {
        customerServiceClient.updateOnboardingStatusToRejected(event.getCustomerId());
        notificationServiceClient.sendApplicationRejected(
            event.getCustomerId(),
            event.getCustomerEmail(),
            event.getReason()
        );
        LOGGER.info("Forwarded application-rejected event to customer-service and notification-service. customerId={}",
            event.getCustomerId());
    }
}
