package com.bank.onboarding.risk.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onboarding.risk.exception.DownstreamIntegrationException;
import com.bank.onboarding.risk.model.RiskAssessment;
import com.bank.onboarding.risk.repository.RiskAssessmentRepository;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/risk-assessments")
public class RiskAssessmentQueryController {

    private final RiskAssessmentRepository riskAssessmentRepository;

    public RiskAssessmentQueryController(RiskAssessmentRepository riskAssessmentRepository) {
        this.riskAssessmentRepository = riskAssessmentRepository;
    }

    @GetMapping("/customer/{customerId}/latest")
    public ResponseEntity<RiskAssessmentResponse> getLatestByCustomerId(@PathVariable String customerId) {
        RiskAssessment assessment = riskAssessmentRepository
                .findTopByCustomerIdOrderByAssessedAtDesc(customerId)
                .orElseThrow(() -> new DownstreamIntegrationException(
                "Risk assessment not found for customerId=" + customerId,
                null));

        return ResponseEntity.ok(new RiskAssessmentResponse(
                assessment.getAssessmentId(),
                assessment.getCustomerId(),
                assessment.getScore(),
                assessment.getDisposition(),
                assessment.getAssessedAt()));
    }
}
