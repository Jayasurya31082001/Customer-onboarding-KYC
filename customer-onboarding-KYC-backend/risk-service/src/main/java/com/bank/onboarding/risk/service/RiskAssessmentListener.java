package com.bank.onboarding.risk.service;

import java.time.LocalDate;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.bank.onboarding.risk.client.CustomerServiceClient;
import com.bank.onboarding.risk.events.ApplicationRejectedEvent;
import com.bank.onboarding.risk.events.KycCompletedEvent;
import com.bank.onboarding.risk.events.ManualReviewRequiredEvent;
import com.bank.onboarding.risk.events.RiskAssessedEvent;
import com.bank.onboarding.risk.model.Disposition;
import com.bank.onboarding.risk.model.RiskAssessment;
import com.bank.onboarding.risk.repository.RiskAssessmentRepository;

@Component
public class RiskAssessmentListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskAssessmentListener.class);

    private final RiskRuleService riskRuleService;
    private final RiskScoringEngine riskScoringEngine;
    private final RiskAssessmentRepository riskAssessmentRepository;
    private final ApplicationEventPublisher applicationEventPublisher;
        private final CustomerServiceClient customerServiceClient;

    public RiskAssessmentListener(RiskRuleService riskRuleService,
                                  RiskScoringEngine riskScoringEngine,
                                  RiskAssessmentRepository riskAssessmentRepository,
                                                                  ApplicationEventPublisher applicationEventPublisher,
                                                                  CustomerServiceClient customerServiceClient) {
        this.riskRuleService = riskRuleService;
        this.riskScoringEngine = riskScoringEngine;
        this.riskAssessmentRepository = riskAssessmentRepository;
        this.applicationEventPublisher = applicationEventPublisher;
                this.customerServiceClient = customerServiceClient;
    }

    @Async("riskTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleKycCompleted(KycCompletedEvent event) {
        LOGGER.info("Starting async risk assessment. thread={}, customerId={}, kycCaseId={}, correlationId={}",
                Thread.currentThread().getName(), event.getCustomerId(), event.getKycCaseId(), event.getCorrelationId());

                String effectiveNationality = event.getNationality();
                LocalDate effectiveDateOfBirth = event.getDateOfBirth();
                String effectiveCustomerEmail = event.getCustomerEmail();

                if (effectiveNationality == null || effectiveNationality.isBlank()
                                || effectiveDateOfBirth == null
                                || effectiveCustomerEmail == null || effectiveCustomerEmail.isBlank()) {
                        CustomerServiceClient.CustomerProfile customerProfile = customerServiceClient.getCustomerProfile(event.getCustomerId().toString());
                        if (customerProfile != null) {
                                if (effectiveNationality == null || effectiveNationality.isBlank()) {
                                        effectiveNationality = customerProfile.nationality();
                                }
                                if (effectiveDateOfBirth == null) {
                                        effectiveDateOfBirth = customerProfile.dateOfBirth();
                                }
                                if (effectiveCustomerEmail == null || effectiveCustomerEmail.isBlank()) {
                                        effectiveCustomerEmail = customerProfile.email();
                                }
                        }
                }

                KycCompletedEvent scoringEvent = new KycCompletedEvent(
                                this,
                                event.getKycCaseId(),
                                event.getCustomerId(),
                                event.getDocumentId(),
                                event.getStatus(),
                                event.getOccurredAt(),
                                event.getCorrelationId(),
                                effectiveNationality,
                                effectiveDateOfBirth,
                                event.isPepMatch(),
                                event.isSanctionsMatch(),
                                effectiveCustomerEmail
                );

                int score = riskScoringEngine.calculate(scoringEvent, riskRuleService.getActiveRiskRules());
        Disposition disposition = riskScoringEngine.scoreToDisposition(score);

        RiskAssessment savedAssessment = riskAssessmentRepository.save(RiskAssessment.builder()
                .customerId(event.getCustomerId().toString())
                .score(score)
                .disposition(disposition)
                .build());

        applicationEventPublisher.publishEvent(new RiskAssessedEvent(
                this,
                savedAssessment.getCustomerId(),
                effectiveCustomerEmail,
                disposition,
                score
        ));

        if (disposition == Disposition.AUTO_REJECT) {
            applicationEventPublisher.publishEvent(new ApplicationRejectedEvent(
                    this,
                    savedAssessment.getCustomerId(),
                    effectiveCustomerEmail,
                    "Risk score exceeded rejection threshold: " + score
            ));
        } else if (disposition == Disposition.MANUAL_REVIEW) {
                        customerServiceClient.updateOnboardingStatusToManualReview(savedAssessment.getCustomerId());
            applicationEventPublisher.publishEvent(new ManualReviewRequiredEvent(
                    this,
                    savedAssessment.getCustomerId(),
                    effectiveCustomerEmail,
                    score,
                    "Application requires manual review based on risk scoring"
            ));
        }

        LOGGER.info("Completed async risk assessment. thread={}, assessmentId={}, customerId={}, score={}, disposition={}, correlationId={}",
                Thread.currentThread().getName(),
                savedAssessment.getAssessmentId(),
                savedAssessment.getCustomerId(),
                score,
                disposition,
                event.getCorrelationId());
    }
}