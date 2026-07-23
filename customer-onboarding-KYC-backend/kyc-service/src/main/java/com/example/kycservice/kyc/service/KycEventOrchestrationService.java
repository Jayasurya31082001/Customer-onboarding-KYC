package com.example.kycservice.kyc.service;

import java.time.LocalDateTime;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kycservice.common.exception.ResourceNotFoundException;
import com.example.kycservice.customer.client.CustomerClient;
import com.example.kycservice.customer.client.CustomerProfile;
import com.example.kycservice.kyc.dto.StartKycRequest;
import com.example.kycservice.kyc.model.KycEventTriggerState;
import com.example.kycservice.kyc.repository.KycEventTriggerStateRepository;

@Service
public class KycEventOrchestrationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(KycEventOrchestrationService.class);

    private final KycEventTriggerStateRepository triggerStateRepository;
    private final CustomerClient customerClient;
    private final KycVerificationService kycVerificationService;

    public KycEventOrchestrationService(KycEventTriggerStateRepository triggerStateRepository,
                                        CustomerClient customerClient,
                                        KycVerificationService kycVerificationService) {
        this.triggerStateRepository = triggerStateRepository;
        this.customerClient = customerClient;
        this.kycVerificationService = kycVerificationService;
    }

    @Transactional
    public void onCustomerRegistered(UUID customerId, LocalDateTime occurredAt, String correlationId) {
        KycEventTriggerState triggerState = triggerStateRepository.findById(customerId)
                .orElseGet(() -> createState(customerId, occurredAt));

        triggerState.setCustomerRegisteredReceived(true);
        triggerState.setUpdatedAt(occurredAt);
        tryStartKyc(triggerState, correlationId);
        triggerStateRepository.save(triggerState);
    }

    @Transactional
    public void onDocumentUploaded(UUID customerId,
                                   UUID documentId,
                                   LocalDateTime occurredAt,
                                   String correlationId) {
        KycEventTriggerState triggerState = triggerStateRepository.findById(customerId)
                .orElseGet(() -> createState(customerId, occurredAt));

        triggerState.setLatestDocumentId(documentId);
        triggerState.setUpdatedAt(occurredAt);

        // Recover from missed customer-registered events by confirming customer existence on demand.
        if (!triggerState.isCustomerRegisteredReceived()) {
            try {
                customerClient.getCustomer(customerId);
                triggerState.setCustomerRegisteredReceived(true);
            } catch (ResourceNotFoundException exception) {
                LOGGER.warn("Skipping KYC start because customer is not available yet. customerId={}, correlationId={}",
                        customerId, correlationId);
            }
        }

        tryStartKyc(triggerState, correlationId);
        triggerStateRepository.save(triggerState);
    }

    @Transactional
    public void processKyc(UUID customerId) {
        onCustomerRegistered(customerId, LocalDateTime.now(), MDC.get("correlationId"));
    }

    private KycEventTriggerState createState(UUID customerId, LocalDateTime occurredAt) {
        KycEventTriggerState state = new KycEventTriggerState();
        state.setCustomerId(customerId);
        state.setCustomerRegisteredReceived(false);
        state.setUpdatedAt(occurredAt);
        return state;
    }

    private void tryStartKyc(KycEventTriggerState triggerState, String correlationId) {
        if (!triggerState.isCustomerRegisteredReceived() || triggerState.getLatestDocumentId() == null) {
            return;
        }

        UUID latestDocumentId = triggerState.getLatestDocumentId();
        if (latestDocumentId.equals(triggerState.getLastStartedDocumentId())) {
            LOGGER.info("Skipping KYC start for already processed document event. customerId={}, documentId={}, correlationId={}",
                    triggerState.getCustomerId(), latestDocumentId, correlationId);
            return;
        }

        CustomerProfile customerProfile = customerClient.getCustomer(triggerState.getCustomerId());
        StartKycRequest request = StartKycRequest.builder()
                .customerId(triggerState.getCustomerId())
                .documentId(latestDocumentId)
                .firstName(customerProfile.firstName())
                .lastName(customerProfile.lastName())
                .dateOfBirth(customerProfile.dateOfBirth())
                .build();

        String idempotencyKey = "EVENT-" + triggerState.getCustomerId() + "-" + latestDocumentId;
        kycVerificationService.startKyc(request, idempotencyKey);
        triggerState.setLastStartedDocumentId(latestDocumentId);

        LOGGER.info("KYC started from upstream events. customerId={}, documentId={}, correlationId={}",
                triggerState.getCustomerId(), latestDocumentId, correlationId);
    }
}
