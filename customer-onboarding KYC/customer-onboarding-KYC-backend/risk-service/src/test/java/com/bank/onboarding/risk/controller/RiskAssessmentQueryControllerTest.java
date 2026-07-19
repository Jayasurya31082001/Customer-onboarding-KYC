package com.bank.onboarding.risk.controller;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.bank.onboarding.risk.model.Disposition;
import com.bank.onboarding.risk.model.RiskAssessment;
import com.bank.onboarding.risk.repository.RiskAssessmentRepository;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentQueryControllerTest {

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    private RiskAssessmentQueryController controller;

    @BeforeEach
    void setUp() {
        controller = new RiskAssessmentQueryController(riskAssessmentRepository);
    }

    @Test
    void getLatestByCustomerId_returnsLatestAssessment() {
        String customerId = "cust-123";
        RiskAssessment assessment = RiskAssessment.builder()
                .assessmentId("assess-1")
                .customerId(customerId)
                .score(62)
                .disposition(Disposition.MANUAL_REVIEW)
                .assessedAt(LocalDateTime.of(2026, 7, 17, 10, 0))
                .build();

        when(riskAssessmentRepository.findTopByCustomerIdOrderByAssessedAtDesc(customerId))
                .thenReturn(Optional.of(assessment));

        var response = controller.getLatestByCustomerId(customerId);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().assessmentId()).isEqualTo("assess-1");
        assertThat(response.getBody().customerId()).isEqualTo(customerId);
        assertThat(response.getBody().score()).isEqualTo(62);
        assertThat(response.getBody().disposition()).isEqualTo(Disposition.MANUAL_REVIEW);
        assertThat(response.getBody().assessedAt()).isEqualTo(LocalDateTime.of(2026, 7, 17, 10, 0));
    }
}
