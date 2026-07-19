package com.bank.onboarding.risk.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;
import org.springframework.context.ApplicationEventPublisher;

import com.bank.onboarding.risk.events.KycCompletedEvent;
import com.bank.onboarding.risk.model.KycStatus;

@ExtendWith(MockitoExtension.class)
class RiskEventIngressServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private RiskEventIngressService service;

    @BeforeEach
    void setUp() {
        service = new RiskEventIngressService(applicationEventPublisher);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void onKycCompleted_usesProvidedCorrelationId() {
        UUID kycCaseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();

        service.onKycCompleted(
                kycCaseId,
                customerId,
                documentId,
                KycStatus.PASS,
                occurredAt,
                "corr-123",
                "IN",
                LocalDate.of(1990, 1, 1),
                false,
                false,
                "user@bank.test");

        ArgumentCaptor<KycCompletedEvent> captor = ArgumentCaptor.forClass(KycCompletedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCorrelationId()).isEqualTo("corr-123");
        assertThat(captor.getValue().getCustomerEmail()).isEqualTo("user@bank.test");
    }

    @Test
    void onKycCompleted_fallsBackToMdcCorrelationIdWhenBlank() {
        UUID kycCaseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        MDC.put("correlationId", "mdc-999");

        service.onKycCompleted(
                kycCaseId,
                customerId,
                documentId,
                KycStatus.PASS,
                occurredAt,
                "  ",
                "US",
                LocalDate.of(1988, 6, 15),
                true,
                false,
                "user2@bank.test");

        ArgumentCaptor<KycCompletedEvent> captor = ArgumentCaptor.forClass(KycCompletedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertThat(captor.getValue().getCorrelationId()).isEqualTo("mdc-999");
    }
}
