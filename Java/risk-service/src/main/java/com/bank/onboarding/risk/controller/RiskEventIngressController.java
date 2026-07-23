package com.bank.onboarding.risk.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.bank.onboarding.risk.service.RiskEventIngressService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/internal/events")
@Validated
public class RiskEventIngressController {

    private static final Logger LOGGER = LoggerFactory.getLogger(RiskEventIngressController.class);

    private final RiskEventIngressService riskEventIngressService;

    public RiskEventIngressController(RiskEventIngressService riskEventIngressService) {
        this.riskEventIngressService = riskEventIngressService;
    }

    @PostMapping("/kyc-completed")
    public ResponseEntity<Void> onKycCompleted(@Valid @RequestBody KycCompletedIngressRequest request) {
        LOGGER.info("Accepted KYC completed event request. customerId={}, kycCaseId={}, correlationId={}",
            request.customerId(), request.kycCaseId(), request.correlationId());

        riskEventIngressService.onKycCompleted(
                request.kycCaseId(),
                request.customerId(),
                request.documentId(),
                request.status(),
                request.occurredAt(),
                request.correlationId(),
                request.nationality(),
                request.dateOfBirth(),
                request.pepMatch(),
                request.sanctionsMatch(),
                request.customerEmail()
        );

            LOGGER.info("KYC completed event request accepted for async processing. customerId={}, kycCaseId={}",
                request.customerId(), request.kycCaseId());

        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}