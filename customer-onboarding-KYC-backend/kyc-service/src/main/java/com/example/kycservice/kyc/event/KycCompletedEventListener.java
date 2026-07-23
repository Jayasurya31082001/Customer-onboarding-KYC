package com.example.kycservice.kyc.event;

import java.util.Locale;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.context.event.EventListener;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;

import com.example.kycservice.customer.client.CustomerClient;
import com.example.kycservice.risk.client.RiskClient;

@Component
public class KycCompletedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycCompletedEventListener.class);
    private static final String KYC_COMPLETED = "KYC_COMPLETED";

    private final CustomerClient customerClient;
    private final RiskClient riskClient;

    public KycCompletedEventListener(CustomerClient customerClient,
                                     RiskClient riskClient) {
        this.customerClient = customerClient;
        this.riskClient = riskClient;
    }

    @CacheEvict(value = "kyc-status", key = "#event.customerId")
    @EventListener
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public void onKycCompleted(KycCompletedEvent event) {
        if (isAlreadyMovedBeyondKycCompleted(event)) {
            LOGGER.info("Skipping KYC completed processing because onboarding status already moved forward. customerId={}, correlationId={}",
                    event.customerId(), event.correlationId());
            return;
        }

        updateCustomerStatusToCompleted(event);
        publishToRiskService(event);
        LOGGER.info("KYC completed successfully. kycCaseId={}, customerId={}, status={}, correlationId={}",
                event.kycCaseId(), event.customerId(), event.status(), event.correlationId());
    }

    private boolean isAlreadyMovedBeyondKycCompleted(KycCompletedEvent event) {
        String currentStatus = customerClient.getCustomer(event.customerId()).onboardingStatus();
        if (currentStatus == null || currentStatus.isBlank()) {
            return false;
        }

        String normalized = currentStatus.trim().toUpperCase(Locale.ROOT);
        return "MANUAL_APPROVAL_REQUIRED".equals(normalized)
                || "APPROVED".equals(normalized)
                || "REJECTED".equals(normalized);
    }

    private void updateCustomerStatusToCompleted(KycCompletedEvent event) {
        customerClient.updateOnboardingStatus(event.customerId(), KYC_COMPLETED, event.correlationId());
        LOGGER.info("Updated onboarding status to {} before risk publish. customerId={}, correlationId={}",
                KYC_COMPLETED, event.customerId(), event.correlationId());
    }

    private void publishToRiskService(KycCompletedEvent event) {
        riskClient.publishKycCompleted(
            event.kycCaseId(),
            event.customerId(),
            event.documentId(),
            event.status(),
            event.occurredAt(),
            event.correlationId(),
            event.nationality(),
            event.dateOfBirth(),
            event.pepMatch(),
            event.sanctionsMatch(),
            event.customerEmail()
        );
    }

    @Recover
    public void recover(Exception exception, KycCompletedEvent event) {
        LOGGER.error("Failed to process KycCompletedEvent after retries. kycCaseId={}, correlationId={}",
                event.kycCaseId(), event.correlationId(), exception);
    }
}

