package com.bank.onboarding.risk.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.bank.onboarding.risk.client.CustomerServiceClient;
import com.bank.onboarding.risk.events.ApplicationRejectedEvent;
import com.bank.onboarding.risk.events.KycCompletedEvent;
import com.bank.onboarding.risk.events.ManualReviewRequiredEvent;
import com.bank.onboarding.risk.events.RiskAssessedEvent;
import com.bank.onboarding.risk.model.Disposition;
import com.bank.onboarding.risk.model.KycStatus;
import com.bank.onboarding.risk.model.RiskAssessment;
import com.bank.onboarding.risk.model.RiskRule;
import com.bank.onboarding.risk.repository.RiskAssessmentRepository;
import com.bank.onboarding.risk.repository.RiskRuleRepository;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentListenerTest {

    @Mock
    private RiskRuleRepository riskRuleRepository;

    @Mock
    private RiskAssessmentRepository riskAssessmentRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @Captor
    private ArgumentCaptor<ApplicationEvent> eventCaptor;

    private RiskScoringEngine riskScoringEngine;

    private RiskRuleService riskRuleService;

    private RiskAssessmentListener riskAssessmentListener;

    @BeforeEach
    void setUp() {
        RiskProperties riskProperties = new RiskProperties();
        riskProperties.setHighRiskNationalities(Set.of("IR"));
        riskScoringEngine = new RiskScoringEngine(riskProperties);
        riskRuleService = new RiskRuleService(riskRuleRepository);
        riskAssessmentListener = new RiskAssessmentListener(
                riskRuleService,
                riskScoringEngine,
                riskAssessmentRepository,
            applicationEventPublisher,
            customerServiceClient
        );
    }

    @Test
    void shouldPublishRiskAssessedAndApplicationRejectedWhenAutoRejected() {
        when(riskRuleRepository.findByActiveTrue()).thenReturn(List.of(rule("KYC_FAIL", 80)));
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> {
            RiskAssessment assessment = invocation.getArgument(0);
            assessment.setAssessmentId("assessment-1");
            return assessment;
        });

        riskAssessmentListener.handleKycCompleted(event(KycStatus.FAIL));

        verify(applicationEventPublisher, times(2)).publishEvent(eventCaptor.capture());

        List<ApplicationEvent> publishedEvents = eventCaptor.getAllValues();
        RiskAssessedEvent assessedEvent = (RiskAssessedEvent) publishedEvents.get(0);
        ApplicationRejectedEvent rejectedEvent = (ApplicationRejectedEvent) publishedEvents.get(1);

        org.assertj.core.api.Assertions.assertThat(assessedEvent.getScore()).isEqualTo(80);
        org.assertj.core.api.Assertions.assertThat(assessedEvent.getDisposition()).isEqualTo(Disposition.AUTO_REJECT);
        org.assertj.core.api.Assertions.assertThat(rejectedEvent.getReason()).contains("80");
    }

    @Test
    void shouldPublishOnlyRiskAssessedWhenAutoApproved() {
        when(riskRuleRepository.findByActiveTrue()).thenReturn(List.of(rule("KYC_FAIL", 80)));
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> {
            RiskAssessment assessment = invocation.getArgument(0);
            assessment.setAssessmentId("assessment-2");
            return assessment;
        });

        riskAssessmentListener.handleKycCompleted(event(KycStatus.PASS));

        verify(applicationEventPublisher).publishEvent(any(RiskAssessedEvent.class));
        verify(applicationEventPublisher, never()).publishEvent(any(ApplicationRejectedEvent.class));
    }

    @Test
    void shouldUpdateCustomerStatusAndPublishManualReviewEventWhenManualReview() {
        when(riskRuleRepository.findByActiveTrue()).thenReturn(List.of(rule("KYC_REFER", 40)));
        when(riskAssessmentRepository.save(any(RiskAssessment.class))).thenAnswer(invocation -> {
            RiskAssessment assessment = invocation.getArgument(0);
            assessment.setAssessmentId("assessment-3");
            return assessment;
        });

        riskAssessmentListener.handleKycCompleted(event(KycStatus.REFER));

        verify(customerServiceClient).updateOnboardingStatusToManualReview(any(String.class));
        verify(applicationEventPublisher).publishEvent(any(RiskAssessedEvent.class));
        verify(applicationEventPublisher).publishEvent(any(ManualReviewRequiredEvent.class));
        verify(applicationEventPublisher, never()).publishEvent(any(ApplicationRejectedEvent.class));
    }

    private RiskRule rule(String ruleName, int riskPoints) {
        return RiskRule.builder()
                .ruleName(ruleName)
                .riskPoints(riskPoints)
                .active(true)
                .build();
    }

    private KycCompletedEvent event(KycStatus status) {
        return new KycCompletedEvent(
                this,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                status,
                LocalDateTime.now(),
                "corr-123",
                null,
                null,
                false,
                false,
                "test@example.com"
        );
    }
}