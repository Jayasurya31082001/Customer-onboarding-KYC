package com.example.kycservice.kyc.event;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThatNoException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import com.example.kycservice.customer.client.CustomerClient;
import com.example.kycservice.customer.client.CustomerProfile;
import com.example.kycservice.kyc.model.KycStatus;
import com.example.kycservice.risk.client.RiskClient;

class KycCompletedEventListenerTest {

    private final CustomerClient customerClient = mock(CustomerClient.class);
    private final RiskClient riskClient = mock(RiskClient.class);
    private final KycCompletedEventListener listener = new KycCompletedEventListener(customerClient, riskClient);

    @Test
    @DisplayName("onKycCompleted logs without throwing")
    void onKycCompleted_logsWithoutThrowing() {
        UUID customerId = UUID.randomUUID();
        KycCompletedEvent event = new KycCompletedEvent(
                UUID.randomUUID(),
                customerId,
                UUID.randomUUID(),
                KycStatus.PASS,
                LocalDateTime.now(),
            "corr-id",
            "GB",
            java.time.LocalDate.of(1990, 1, 1),
            false,
            false,
            "john.doe@example.com"
        );
        
        CustomerProfile customerProfile = new CustomerProfile(
                customerId,
                "John",
                "Doe",
                java.time.LocalDate.of(1990, 1, 1),
                "GB",
                "KYC_IN_PROGRESS",
                "john.doe@example.com"
        );
            when(customerClient.getCustomer(customerId)).thenReturn(customerProfile);

        assertThatNoException().isThrownBy(() -> listener.onKycCompleted(event));
        InOrder inOrder = inOrder(customerClient, riskClient);
        inOrder.verify(customerClient).getCustomer(event.customerId());
        inOrder.verify(customerClient).updateOnboardingStatus(event.customerId(), "KYC_COMPLETED", event.correlationId());
        inOrder.verify(riskClient).publishKycCompleted(
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
            "john.doe@example.com"
        );
        inOrder.verifyNoMoreInteractions();
    }

    @Test
    @DisplayName("onKycCompleted skips when onboarding already moved to manual approval required")
    void onKycCompleted_skipsWhenAlreadyManualApprovalRequired() {
        UUID customerId = UUID.randomUUID();
        KycCompletedEvent event = new KycCompletedEvent(
                UUID.randomUUID(),
                customerId,
                UUID.randomUUID(),
                KycStatus.PASS,
                LocalDateTime.now(),
                "corr-id-2",
                "GB",
                java.time.LocalDate.of(1990, 1, 1),
                false,
                false,
                "john.doe@example.com"
        );

        when(customerClient.getCustomer(customerId)).thenReturn(new CustomerProfile(
                customerId,
                "John",
                "Doe",
                java.time.LocalDate.of(1990, 1, 1),
                "GB",
                "MANUAL_APPROVAL_REQUIRED",
                "john.doe@example.com"
        ));

        assertThatNoException().isThrownBy(() -> listener.onKycCompleted(event));

        verify(customerClient).getCustomer(customerId);
        verifyNoInteractions(riskClient);
    }

    @Test
    @DisplayName("recover handles failure path without throwing")
    void recover_handlesFailurePathWithoutThrowing() {
        KycCompletedEvent event = new KycCompletedEvent(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                KycStatus.PASS,
                LocalDateTime.now(),
            "corr-id",
            "GB",
            java.time.LocalDate.of(1990, 1, 1),
            false,
            false,
            "john.doe@example.com"
        );

        assertThatNoException().isThrownBy(() -> listener.recover(new RuntimeException("boom"), event));
    }
}
