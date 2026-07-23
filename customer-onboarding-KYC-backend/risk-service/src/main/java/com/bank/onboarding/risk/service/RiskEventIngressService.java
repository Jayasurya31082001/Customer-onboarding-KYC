package com.bank.onboarding.risk.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.bank.onboarding.risk.events.KycCompletedEvent;
import com.bank.onboarding.risk.model.KycStatus;

@Service
public class RiskEventIngressService {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskEventIngressService.class);

    private final ApplicationEventPublisher applicationEventPublisher;

    public RiskEventIngressService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void onKycCompleted(UUID kycCaseId,
                               UUID customerId,
                               UUID documentId,
                               KycStatus status,
                               LocalDateTime occurredAt,
                               String correlationId,
                               String nationality,
                               LocalDate dateOfBirth,
                               boolean pepMatch,
                               boolean sanctionsMatch,
                               String customerEmail) {
        String effectiveCorrelationId = correlationId == null || correlationId.isBlank()
                ? MDC.get("correlationId")
                : correlationId;

        LOGGER.info("Received KYC completed ingress. customerId={}, kycCaseId={}, correlationId={}",
            customerId, kycCaseId, effectiveCorrelationId);

        applicationEventPublisher.publishEvent(new KycCompletedEvent(
                this,
                kycCaseId,
                customerId,
                documentId,
                status,
                occurredAt,
                effectiveCorrelationId,
                nationality,
                dateOfBirth,
                pepMatch,
                sanctionsMatch,
                customerEmail
        ));

            LOGGER.info("Published KYC completed event for async risk processing. customerId={}, kycCaseId={}, correlationId={}",
                customerId, kycCaseId, effectiveCorrelationId);
    }
}