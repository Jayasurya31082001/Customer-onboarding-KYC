package com.bank.onboarding.risk.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.bank.onboarding.risk.model.KycStatus;
import com.bank.onboarding.risk.service.RiskEventIngressService;

@ExtendWith(MockitoExtension.class)
class RiskEventIngressControllerTest {

    @Mock
    private RiskEventIngressService riskEventIngressService;

    private RiskEventIngressController controller;

    @BeforeEach
    void setUp() {
        controller = new RiskEventIngressController(riskEventIngressService);
    }

    @Test
    void onKycCompleted_delegatesAndReturnsAccepted() {
        UUID kycCaseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();

        KycCompletedIngressRequest request = new KycCompletedIngressRequest(
                kycCaseId,
                customerId,
                documentId,
                KycStatus.PASS,
                occurredAt,
                "corr-123",
                "US",
                LocalDate.of(1991, 3, 10),
                false,
                false,
                "user@bank.test");

        var response = controller.onKycCompleted(request);

        verify(riskEventIngressService).onKycCompleted(
                kycCaseId,
                customerId,
                documentId,
                KycStatus.PASS,
                occurredAt,
                "corr-123",
                "US",
                LocalDate.of(1991, 3, 10),
                false,
                false,
                "user@bank.test");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}
