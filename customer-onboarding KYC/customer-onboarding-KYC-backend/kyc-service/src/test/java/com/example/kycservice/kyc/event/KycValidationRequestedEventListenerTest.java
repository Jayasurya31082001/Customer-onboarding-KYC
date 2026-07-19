package com.example.kycservice.kyc.event;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.kycservice.common.exception.ResourceNotFoundException;
import com.example.kycservice.customer.client.CustomerClient;
import com.example.kycservice.customer.client.CustomerProfile;
import com.example.kycservice.document.client.DocumentClient;
import com.example.kycservice.document.client.DocumentDetails;
import com.example.kycservice.kyc.model.KycCase;
import com.example.kycservice.kyc.model.KycStatus;
import com.example.kycservice.kyc.provider.KycProviderService;
import com.example.kycservice.kyc.repository.KycCaseRepository;

@ExtendWith(MockitoExtension.class)
class KycValidationRequestedEventListenerTest {

        private static final String KYC_IN_PROGRESS = "KYC_IN_PROGRESS";

    @Mock
    private KycCaseRepository kycCaseRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private DocumentClient documentClient;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private KycValidationRequestedEventListener listener;

        @BeforeEach
        private void setUp() {
                listener = new KycValidationRequestedEventListener(
                                kycCaseRepository,
                                customerClient,
                                documentClient,
                                new KycProviderService(),
                                applicationEventPublisher);
        }

    @Test
    @DisplayName("onKycValidationRequested sets status Pass and publishes completed event")
    void onKycValidationRequested_setsPass() {
        UUID caseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        KycCase kycCase = buildCase(caseId, customerId, documentId, "Alice", "Walker");
        when(kycCaseRepository.findById(caseId)).thenReturn(Optional.of(kycCase));
        when(customerClient.getCustomer(customerId)).thenReturn(new CustomerProfile(
                customerId,
                "Alice",
                "Walker",
                LocalDate.of(1990, 2, 14),
                "GB",
                "PENDING",
                "alice@example.com"
        ));
        when(documentClient.getDocumentDetails(documentId)).thenReturn(new DocumentDetails(
                documentId,
                customerId,
                "alice_walker.pdf",
                "application/pdf",
                102400
        ));

        listener.onKycValidationRequested(new KycValidationRequestedEvent(
                caseId,
                customerId,
                documentId,
                LocalDateTime.now(),
                "corr-1"
        ));

        verify(customerClient).updateOnboardingStatus(customerId, KYC_IN_PROGRESS, "corr-1");
        verify(kycCaseRepository).save(kycCase);
        verify(applicationEventPublisher).publishEvent(any(KycCompletedEvent.class));
    }

    @Test
    @DisplayName("onKycValidationRequested sets status Fail when mismatch")
    void onKycValidationRequested_setsFailOnMismatch() {
        UUID caseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        KycCase kycCase = buildCase(caseId, customerId, documentId, "Alice", "Walker");
        when(kycCaseRepository.findById(caseId)).thenReturn(Optional.of(kycCase));
        when(customerClient.getCustomer(customerId)).thenReturn(new CustomerProfile(
                customerId,
                "Bob",
                "Walker",
                LocalDate.of(1990, 2, 14),
                "GB",
                "PENDING",
                "bob@example.com"
        ));
        when(documentClient.getDocumentDetails(documentId)).thenReturn(new DocumentDetails(
                documentId,
                customerId,
                "alice_walker.pdf",
                "application/pdf",
                102400
        ));

        listener.onKycValidationRequested(new KycValidationRequestedEvent(
                caseId,
                customerId,
                documentId,
                LocalDateTime.now(),
                "corr-2"
        ));

        verify(customerClient).updateOnboardingStatus(customerId, KYC_IN_PROGRESS, "corr-2");
        verify(kycCaseRepository).save(kycCase);
        verify(applicationEventPublisher, never()).publishEvent(any(KycCompletedEvent.class));
        verify(applicationEventPublisher).publishEvent(any(ApplicationRejectedEvent.class));
    }

    @Test
    @DisplayName("onKycValidationRequested sets status Fail when document name doesn't match")
    void onKycValidationRequested_setsFailOnDocumentNameMismatch() {
        UUID caseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        KycCase kycCase = buildCase(caseId, customerId, documentId, "Alice", "Walker");
        when(kycCaseRepository.findById(caseId)).thenReturn(Optional.of(kycCase));
        when(customerClient.getCustomer(customerId)).thenReturn(new CustomerProfile(
                customerId,
                "Alice",
                "Walker",
                LocalDate.of(1990, 2, 14),
                "GB",
                "PENDING",
                "alice@example.com"
        ));
        when(documentClient.getDocumentDetails(documentId)).thenReturn(new DocumentDetails(
                documentId,
                customerId,
                "wrong_name.pdf",
                "application/pdf",
                102400
        ));

        listener.onKycValidationRequested(new KycValidationRequestedEvent(
                caseId,
                customerId,
                documentId,
                LocalDateTime.now(),
                "corr-doc-mismatch"
        ));

        verify(customerClient).updateOnboardingStatus(customerId, KYC_IN_PROGRESS, "corr-doc-mismatch");
        verify(kycCaseRepository).save(kycCase);
        verify(applicationEventPublisher, never()).publishEvent(any(KycCompletedEvent.class));
        verify(applicationEventPublisher).publishEvent(any(ApplicationRejectedEvent.class));
    }

    @Test
    @DisplayName("onKycValidationRequested sets status Fail when resource is missing")
    void onKycValidationRequested_setsFailOnMissingResource() {
        UUID caseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();

        KycCase kycCase = buildCase(caseId, customerId, documentId, "Alice", "Walker");
        when(kycCaseRepository.findById(caseId)).thenReturn(Optional.of(kycCase));
        when(customerClient.getCustomer(customerId))
                .thenThrow(new ResourceNotFoundException("Customer not found"));

        listener.onKycValidationRequested(new KycValidationRequestedEvent(
                caseId,
                customerId,
                documentId,
                LocalDateTime.now(),
                "corr-3"
        ));

        verify(customerClient).updateOnboardingStatus(customerId, KYC_IN_PROGRESS, "corr-3");
        verify(kycCaseRepository).save(kycCase);
        verify(applicationEventPublisher, never()).publishEvent(any(KycCompletedEvent.class));
    }

    @Test
    @DisplayName("recover marks case as Fail and does not throw")
    void recover_marksFail() {
        UUID caseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        KycCase kycCase = buildCase(caseId, customerId, UUID.randomUUID(), "Alice", "Walker");
        when(kycCaseRepository.findById(caseId)).thenReturn(Optional.of(kycCase));
        when(customerClient.getCustomer(customerId)).thenReturn(new CustomerProfile(
                customerId,
                "Alice",
                "Walker",
                LocalDate.of(1990, 2, 14),
                "GB",
                "PENDING",
                "alice@example.com"
        ));

        assertThatNoException().isThrownBy(() -> listener.recover(
                new RuntimeException("boom"),
                new KycValidationRequestedEvent(caseId, kycCase.getCustomerId(), kycCase.getDocumentId(),
                        LocalDateTime.now(), "corr-4")
        ));

        verify(customerClient, never()).updateOnboardingStatus(any(), any(), any());
        verify(kycCaseRepository).save(kycCase);
        verify(applicationEventPublisher).publishEvent(any(ApplicationRejectedEvent.class));
    }

        @Test
        @DisplayName("onKycValidationRequested ignores unknown kyc case")
        void onKycValidationRequested_unknownCase_isIgnored() {
                UUID caseId = UUID.randomUUID();
                when(kycCaseRepository.findById(caseId)).thenReturn(Optional.empty());

                listener.onKycValidationRequested(new KycValidationRequestedEvent(
                                caseId,
                                UUID.randomUUID(),
                                UUID.randomUUID(),
                                LocalDateTime.now(),
                                "corr-5"
                ));

                verify(kycCaseRepository, never()).save(any(KycCase.class));
                verify(applicationEventPublisher, never()).publishEvent(any());
        }

        @Test
        @DisplayName("onKycValidationRequested skips already finalized case")
        void onKycValidationRequested_finalizedCase_isSkipped() {
                UUID caseId = UUID.randomUUID();
                KycCase kycCase = buildCase(caseId, UUID.randomUUID(), UUID.randomUUID(), "Alice", "Walker");
                kycCase.setStatus(KycStatus.PASS);
                when(kycCaseRepository.findById(caseId)).thenReturn(Optional.of(kycCase));

                listener.onKycValidationRequested(new KycValidationRequestedEvent(
                                caseId,
                                kycCase.getCustomerId(),
                                kycCase.getDocumentId(),
                                LocalDateTime.now(),
                                "corr-6"
                ));

                verify(customerClient, never()).updateOnboardingStatus(any(), any(), any());
                verify(customerClient, never()).getCustomer(any());
                verify(documentClient, never()).getDocumentDetails(any());
                verify(kycCaseRepository, never()).save(any(KycCase.class));
        }

    private KycCase buildCase(UUID caseId, UUID customerId, UUID documentId, String firstName, String lastName) {
        return KycCase.builder()
                .kycCaseId(caseId)
                .customerId(customerId)
                .documentId(documentId)
                .idempotencyKey("idempotency-" + caseId)
                .providedFirstName(firstName)
                .providedLastName(lastName)
                .providedDateOfBirth(LocalDate.of(1990, 2, 14))
                .status(KycStatus.KYC_IN_PROGRESS)
                .notes("KYC accepted for asynchronous validation")
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}
