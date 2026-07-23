package com.example.kycservice.kyc.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kycservice.customer.client.CustomerClient;
import com.example.kycservice.customer.client.CustomerProfile;
import com.example.kycservice.kyc.dto.StartKycRequest;
import com.example.kycservice.kyc.model.KycEventTriggerState;
import com.example.kycservice.kyc.repository.KycEventTriggerStateRepository;

@ExtendWith(MockitoExtension.class)
class KycEventOrchestrationServiceTest {

    @Mock
    private KycEventTriggerStateRepository triggerStateRepository;

    @Mock
    private CustomerClient customerClient;

    @Mock
    private KycVerificationService kycVerificationService;

    @InjectMocks
    private KycEventOrchestrationService orchestrationService;

    @Captor
    private ArgumentCaptor<KycEventTriggerState> stateCaptor;

    @Captor
    private ArgumentCaptor<StartKycRequest> requestCaptor;

    @Test
    @DisplayName("document-uploaded event starts KYC when customer exists even if customer event was missed")
    void onDocumentUploaded_withoutCustomerRegistered_startsKycWhenCustomerExists() {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        when(triggerStateRepository.findById(customerId)).thenReturn(Optional.empty());
        when(customerClient.getCustomer(customerId)).thenReturn(new CustomerProfile(
                customerId,
                "Alice",
                "Walker",
                LocalDate.of(1990, 2, 14),
            "GB",
                "PENDING",
                "alice@example.com"
        ));

        orchestrationService.onDocumentUploaded(customerId, documentId, LocalDateTime.now(), "corr-1");

        verify(kycVerificationService).startKyc(requestCaptor.capture(), eq("EVENT-" + customerId + "-" + documentId));
        StartKycRequest request = requestCaptor.getValue();
        assertThat(request.customerId()).isEqualTo(customerId);
        assertThat(request.documentId()).isEqualTo(documentId);
        assertThat(request.firstName()).isEqualTo("Alice");
        assertThat(request.lastName()).isEqualTo("Walker");
        assertThat(request.dateOfBirth()).isEqualTo(LocalDate.of(1990, 2, 14));

        verify(triggerStateRepository).save(stateCaptor.capture());
        KycEventTriggerState savedState = stateCaptor.getValue();
        assertThat(savedState.getCustomerId()).isEqualTo(customerId);
        assertThat(savedState.isCustomerRegisteredReceived()).isTrue();
        assertThat(savedState.getLatestDocumentId()).isEqualTo(documentId);
        assertThat(savedState.getLastStartedDocumentId()).isEqualTo(documentId);
    }

    @Test
    @DisplayName("customer-registered event starts KYC when document event already exists")
    void onCustomerRegistered_withPendingDocument_startsKyc() {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        KycEventTriggerState state = new KycEventTriggerState();
        state.setCustomerId(customerId);
        state.setCustomerRegisteredReceived(false);
        state.setLatestDocumentId(documentId);
        state.setUpdatedAt(LocalDateTime.now());

        when(triggerStateRepository.findById(customerId)).thenReturn(Optional.of(state));
        when(customerClient.getCustomer(customerId)).thenReturn(new CustomerProfile(
                customerId,
                "Alice",
                "Walker",
                LocalDate.of(1990, 2, 14),
            "GB",
                "PENDING",
                "alice@example.com"
        ));

        orchestrationService.onCustomerRegistered(customerId, LocalDateTime.now(), "corr-2");

        verify(kycVerificationService).startKyc(requestCaptor.capture(), eq("EVENT-" + customerId + "-" + documentId));
        StartKycRequest request = requestCaptor.getValue();
        assertThat(request.customerId()).isEqualTo(customerId);
        assertThat(request.documentId()).isEqualTo(documentId);
        assertThat(request.firstName()).isEqualTo("Alice");
        assertThat(request.lastName()).isEqualTo("Walker");
        assertThat(request.dateOfBirth()).isEqualTo(LocalDate.of(1990, 2, 14));

        verify(triggerStateRepository).save(stateCaptor.capture());
        assertThat(stateCaptor.getValue().getLastStartedDocumentId()).isEqualTo(documentId);
    }

    @Test
    @DisplayName("document-uploaded event is ignored when same document was already started")
    void onDocumentUploaded_sameDocumentAlreadyStarted_skips() {
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        KycEventTriggerState state = new KycEventTriggerState();
        state.setCustomerId(customerId);
        state.setCustomerRegisteredReceived(true);
        state.setLatestDocumentId(documentId);
        state.setLastStartedDocumentId(documentId);
        state.setUpdatedAt(LocalDateTime.now());

        when(triggerStateRepository.findById(customerId)).thenReturn(Optional.of(state));

        orchestrationService.onDocumentUploaded(customerId, documentId, LocalDateTime.now(), "corr-3");

        verify(kycVerificationService, never()).startKyc(any(), any());
        verify(triggerStateRepository).save(stateCaptor.capture());
        assertThat(stateCaptor.getValue().getLastStartedDocumentId()).isEqualTo(documentId);
    }
}
