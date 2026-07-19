package com.example.kycservice.kyc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.kycservice.common.exception.KycValidationException;
import com.example.kycservice.kyc.dto.StartKycRequest;
import com.example.kycservice.kyc.event.KycValidationRequestedEvent;
import com.example.kycservice.kyc.model.KycCase;
import com.example.kycservice.kyc.model.KycStatus;
import com.example.kycservice.kyc.repository.KycCaseRepository;

@ExtendWith(MockitoExtension.class)
class DefaultKycVerificationServiceTest {

    @Mock
    private KycCaseRepository kycCaseRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DefaultKycVerificationService kycVerificationService;

    @Test
        @DisplayName("startKyc publishes async validation event")
        void startKyc_publishesValidationEvent() {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        StartKycRequest request = StartKycRequest.builder()
                .customerId(customerId)
                .documentId(documentId)
                .firstName("Alice")
                .lastName("Walker")
                .dateOfBirth(LocalDate.of(1990, 2, 14))
                .build();

        when(kycCaseRepository.findByIdempotencyKey(any())).thenReturn(Optional.empty());
        when(kycCaseRepository.save(any(KycCase.class))).thenAnswer(invocation -> invocation.getArgument(0));

                kycVerificationService.startKyc(request, null);

        verify(applicationEventPublisher).publishEvent(any(KycValidationRequestedEvent.class));
    }

    @Test
        @DisplayName("startKyc duplicate idempotency key skips duplicate create")
        void startKyc_duplicateIdempotencyKey_skipsDuplicateCreate() {
        UUID caseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        StartKycRequest request = StartKycRequest.builder()
                .customerId(customerId)
                .documentId(documentId)
                .firstName("Alice")
                .lastName("Walker")
                .dateOfBirth(LocalDate.of(1990, 2, 14))
                .build();

        KycCase existing = KycCase.builder()
                .kycCaseId(caseId)
                .customerId(customerId)
                .documentId(documentId)
                .idempotencyKey("req-123")
                .providedFirstName("Alice")
                .providedLastName("Walker")
                .providedDateOfBirth(LocalDate.of(1990, 2, 14))
                .status(KycStatus.KYC_IN_PROGRESS)
                .notes("KYC accepted for asynchronous validation")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(kycCaseRepository.findByIdempotencyKey("req-123")).thenReturn(Optional.of(existing));

                kycVerificationService.startKyc(request, "req-123");

        verify(kycCaseRepository, never()).save(any(KycCase.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("startKyc duplicate idempotency key with different payload throws validation")
    void startKyc_duplicateIdempotencyKeyDifferentPayload_throwsValidation() {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        StartKycRequest request = StartKycRequest.builder()
                .customerId(customerId)
                .documentId(documentId)
                .firstName("Alice")
                .lastName("Walker")
                .dateOfBirth(LocalDate.of(1990, 2, 14))
                .build();

        KycCase existing = KycCase.builder()
                .kycCaseId(UUID.randomUUID())
                .customerId(customerId)
                .documentId(documentId)
                .idempotencyKey("req-123")
                .providedFirstName("Alice")
                .providedLastName("Smith")
                .providedDateOfBirth(LocalDate.of(1990, 2, 14))
                .status(KycStatus.FAIL)
                .notes("KYC failed")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(kycCaseRepository.findByIdempotencyKey("req-123")).thenReturn(Optional.of(existing));

        assertThatThrownBy(() -> kycVerificationService.startKyc(request, "req-123"))
                .isInstanceOf(KycValidationException.class)
                .hasMessageContaining("Idempotency key already used");
    }

    @Test
        @DisplayName("startKyc handles concurrent duplicate save by skipping duplicate create")
        void startKyc_concurrentDuplicateSave_skipsDuplicateCreate() {
        UUID caseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        StartKycRequest request = StartKycRequest.builder()
                .customerId(customerId)
                .documentId(documentId)
                .firstName("Alice")
                .lastName("Walker")
                .dateOfBirth(LocalDate.of(1990, 2, 14))
                .build();

        KycCase existing = KycCase.builder()
                .kycCaseId(caseId)
                .customerId(customerId)
                .documentId(documentId)
                .idempotencyKey("req-123")
                .providedFirstName("Alice")
                .providedLastName("Walker")
                .providedDateOfBirth(LocalDate.of(1990, 2, 14))
                .status(KycStatus.KYC_IN_PROGRESS)
                .notes("KYC accepted for asynchronous validation")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        when(kycCaseRepository.findByIdempotencyKey("req-123"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.of(existing));
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(kycCaseRepository).save(any(KycCase.class));

        kycVerificationService.startKyc(request, "req-123");

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("startKyc rethrows duplicate save when fallback lookup is empty")
    void startKyc_duplicateSaveWithoutFallback_throwsDataIntegrityViolation() {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        StartKycRequest request = StartKycRequest.builder()
                .customerId(customerId)
                .documentId(documentId)
                .firstName("Alice")
                .lastName("Walker")
                .dateOfBirth(LocalDate.of(1990, 2, 14))
                .build();

        when(kycCaseRepository.findByIdempotencyKey("req-123"))
                .thenReturn(Optional.empty())
                .thenReturn(Optional.empty());
        doThrow(new DataIntegrityViolationException("duplicate"))
                .when(kycCaseRepository).save(any(KycCase.class));

        assertThatThrownBy(() -> kycVerificationService.startKyc(request, "req-123"))
                .isInstanceOf(DataIntegrityViolationException.class);
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

}
