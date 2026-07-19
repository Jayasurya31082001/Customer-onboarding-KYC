package com.example.kycservice.kyc.service;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.kycservice.common.exception.KycValidationException;
import com.example.kycservice.kyc.dto.StartKycRequest;
import com.example.kycservice.kyc.event.KycValidationRequestedEvent;
import com.example.kycservice.kyc.model.KycCase;
import com.example.kycservice.kyc.model.KycStatus;
import com.example.kycservice.kyc.repository.KycCaseRepository;

@Service
public class DefaultKycVerificationService implements KycVerificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultKycVerificationService.class);

    private final KycCaseRepository kycCaseRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DefaultKycVerificationService(KycCaseRepository kycCaseRepository,
                                         ApplicationEventPublisher applicationEventPublisher) {
        this.kycCaseRepository = kycCaseRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional
        public void startKyc(StartKycRequest request, String idempotencyKey) {
                String effectiveIdempotencyKey = resolveIdempotencyKey(request, idempotencyKey);
                Optional<KycCase> existingCase = kycCaseRepository.findByIdempotencyKey(effectiveIdempotencyKey);
                if (existingCase.isPresent()) {
                        KycCase kycCase = existingCase.get();
                        if (!sameRequest(kycCase, request)) {
                                throw new KycValidationException("Idempotency key already used for a different request");
                        }
                        LOGGER.info("Idempotent replay detected. idempotencyKey={}, kycCaseId={}",
                                        effectiveIdempotencyKey, kycCase.getKycCaseId());
                        return;
                }

        LocalDateTime now = LocalDateTime.now();
        KycCase kycCase = KycCase.builder()
                .kycCaseId(UUID.randomUUID())
                .customerId(request.customerId())
                .documentId(request.documentId())
                .idempotencyKey(effectiveIdempotencyKey)
                .providedFirstName(request.firstName().trim())
                .providedLastName(request.lastName().trim())
                .providedDateOfBirth(request.dateOfBirth())
                .status(KycStatus.KYC_IN_PROGRESS)
                .notes("KYC accepted for asynchronous validation")
                .createdAt(now)
                .updatedAt(now)
                .build();

        KycCase persistedCase;
        try {
            persistedCase = kycCaseRepository.save(kycCase);
        } catch (DataIntegrityViolationException exception) {
            Optional<KycCase> concurrentCase = kycCaseRepository.findByIdempotencyKey(effectiveIdempotencyKey);
            if (concurrentCase.isPresent()) {
                return;
            }
            throw exception;
        }

        String correlationId = MDC.get("correlationId");
        applicationEventPublisher.publishEvent(new KycValidationRequestedEvent(
                persistedCase.getKycCaseId(),
                persistedCase.getCustomerId(),
                persistedCase.getDocumentId(),
                now,
                correlationId
        ));

        LOGGER.info("KYC accepted for async processing. kycCaseId={}, customerId={}, status={}, correlationId={}",
                persistedCase.getKycCaseId(), persistedCase.getCustomerId(), persistedCase.getStatus(), correlationId);
    }

    private boolean sameRequest(KycCase kycCase, StartKycRequest request) {
        return requestFingerprint(
                kycCase.getCustomerId(),
                kycCase.getDocumentId(),
                kycCase.getProvidedFirstName(),
                kycCase.getProvidedLastName(),
                kycCase.getProvidedDateOfBirth().toString())
                .equals(requestFingerprint(
                        request.customerId(),
                        request.documentId(),
                        request.firstName(),
                        request.lastName(),
                        request.dateOfBirth().toString()));
    }

    private String normalize(String value) {
        return Objects.toString(value, "").trim().toLowerCase(Locale.ROOT);
    }

    private String requestFingerprint(UUID customerId,
                                      UUID documentId,
                                      String firstName,
                                      String lastName,
                                      String dateOfBirth) {
        return customerId + "|"
                + documentId + "|"
                + normalize(firstName) + "|"
                + normalize(lastName) + "|"
                + dateOfBirth;
    }

    private String resolveIdempotencyKey(StartKycRequest request, String idempotencyKey) {
        if (idempotencyKey != null && !idempotencyKey.isBlank()) {
            return idempotencyKey.trim();
        }

        String payload = requestFingerprint(
                request.customerId(),
                request.documentId(),
                request.firstName(),
                request.lastName(),
                request.dateOfBirth().toString());
        return "AUTO-" + UUID.nameUUIDFromBytes(payload.getBytes(StandardCharsets.UTF_8));
    }
}
