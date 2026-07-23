package com.example.kycservice.kyc.event;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.kycservice.common.exception.ResourceNotFoundException;
import com.example.kycservice.customer.client.CustomerClient;
import com.example.kycservice.customer.client.CustomerProfile;
import com.example.kycservice.document.client.DocumentClient;
import com.example.kycservice.kyc.model.KycCase;
import com.example.kycservice.kyc.model.KycStatus;
import com.example.kycservice.kyc.provider.KycProviderService;
import com.example.kycservice.kyc.provider.KycResult;
import com.example.kycservice.kyc.repository.KycCaseRepository;

@Component
public class KycValidationRequestedEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycValidationRequestedEventListener.class);
    private static final String KYC_IN_PROGRESS = "KYC_IN_PROGRESS";

    private final KycCaseRepository kycCaseRepository;
    private final CustomerClient customerClient;
    private final DocumentClient documentClient;
    private final KycProviderService kycProviderService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public KycValidationRequestedEventListener(KycCaseRepository kycCaseRepository,
                                               CustomerClient customerClient,
                                               DocumentClient documentClient,
                                               KycProviderService kycProviderService,
                                               ApplicationEventPublisher applicationEventPublisher) {
        this.kycCaseRepository = kycCaseRepository;
        this.customerClient = customerClient;
        this.documentClient = documentClient;
        this.kycProviderService = kycProviderService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public void onKycValidationRequested(KycValidationRequestedEvent event) {
        KycCase kycCase = kycCaseRepository.findById(event.kycCaseId())
                .orElse(null);
        CustomerProfile customer = null;

        if (kycCase == null) {
            LOGGER.warn("KYC case not found for async validation. kycCaseId={}", event.kycCaseId());
            return;
        }

        if (kycCase.getStatus() != KycStatus.KYC_IN_PROGRESS) {
            LOGGER.info("Skipping async validation for finalized KYC case. kycCaseId={}, status={}",
                    kycCase.getKycCaseId(), kycCase.getStatus());
            return;
        }

        try {
            customerClient.updateOnboardingStatus(kycCase.getCustomerId(), KYC_IN_PROGRESS, event.correlationId());
            customer = customerClient.getCustomer(kycCase.getCustomerId());
            var documentDetails = documentClient.getDocumentDetails(kycCase.getDocumentId());

            KycResult providerResult = kycProviderService.callKycProvider(kycCase, customer, documentDetails);
            kycCase.setStatus(providerResult.status() == KycResult.Status.PASS ? KycStatus.PASS : KycStatus.FAIL);
            LOGGER.info("Async KYC validation result. kycCaseId={}, status={}, correlationId={}",
                    kycCase.getKycCaseId(), kycCase.getStatus(), event.correlationId());
            kycCase.setNotes(providerResult.reason());
            
            kycCaseRepository.save(kycCase);

            if (providerResult.status() == KycResult.Status.PASS) {
                applicationEventPublisher.publishEvent(new KycCompletedEvent(
                        kycCase.getKycCaseId(),
                        kycCase.getCustomerId(),
                        kycCase.getDocumentId(),
                        kycCase.getStatus(),
                        LocalDateTime.now(),
                        event.correlationId(),
                        customer.nationality(),
                        customer.dateOfBirth() != null ? customer.dateOfBirth() : kycCase.getProvidedDateOfBirth(),
                        false,
                        false,
                        customer.email()
                ));
            } else {
                applicationEventPublisher.publishEvent(new ApplicationRejectedEvent(
                        kycCase.getCustomerId(),
                        customer.email(),
                        providerResult.reason(),
                        event.correlationId()
                ));
            }

            LOGGER.info("Async KYC validation completed. kycCaseId={}, status={}, correlationId={}",
                    kycCase.getKycCaseId(), kycCase.getStatus(), event.correlationId());
        } catch (ResourceNotFoundException exception) {
            kycCase.setStatus(KycStatus.FAIL);
            kycCase.setNotes("KYC failed. " + exception.getMessage());
            kycCaseRepository.save(kycCase);
            if (customer != null) {
                applicationEventPublisher.publishEvent(new ApplicationRejectedEvent(
                        kycCase.getCustomerId(),
                        customer.email(),
                        kycCase.getNotes(),
                        event.correlationId()
                ));
            }
            LOGGER.warn("Async KYC validation failed due missing resource. kycCaseId={}, message={}",
                    kycCase.getKycCaseId(), exception.getMessage());
        }
    }

    @Recover
    @Transactional
    public void recover(Exception exception, KycValidationRequestedEvent event) {
        kycCaseRepository.findById(event.kycCaseId()).ifPresent(kycCase -> {
            kycCase.setStatus(KycStatus.FAIL);
            kycCase.setNotes("KYC validation failed after retries");
            kycCaseRepository.save(kycCase);
            try {
                CustomerProfile customer = customerClient.getCustomer(kycCase.getCustomerId());
                applicationEventPublisher.publishEvent(new ApplicationRejectedEvent(
                        kycCase.getCustomerId(),
                        customer.email(),
                        kycCase.getNotes(),
                        event.correlationId()
                ));
            } catch (ResourceNotFoundException ignored) {
                LOGGER.warn("Skipping rejection notification from recover because customer was not found. customerId={}",
                        kycCase.getCustomerId());
            }
        });

        LOGGER.error("Failed to process KycValidationRequestedEvent after retries. kycCaseId={}, correlationId={}",
                event.kycCaseId(), event.correlationId(), exception);
    }

}
