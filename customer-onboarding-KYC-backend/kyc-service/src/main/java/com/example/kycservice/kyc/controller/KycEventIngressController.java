package com.example.kycservice.kyc.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.example.kycservice.kyc.dto.CustomerRegisteredIngressRequest;
import com.example.kycservice.kyc.dto.DocumentUploadedIngressRequest;
import com.example.kycservice.kyc.service.KycEventOrchestrationService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/internal/events")
@Validated

public class KycEventIngressController {

    private final KycEventOrchestrationService orchestrationService;

    public KycEventIngressController(KycEventOrchestrationService orchestrationService) {
        this.orchestrationService = orchestrationService;
    }

    @PostMapping("/customer-registered")
    public ResponseEntity<Void> onCustomerRegistered(@Valid @RequestBody CustomerRegisteredIngressRequest request) {
        orchestrationService.onCustomerRegistered(request.customerId(), request.occurredAt(), request.correlationId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/document-uploaded")
    public ResponseEntity<Void> onDocumentUploaded(@Valid @RequestBody DocumentUploadedIngressRequest request) {
        orchestrationService.onDocumentUploaded(
                request.customerId(),
                request.documentId(),
                request.occurredAt(),
                request.correlationId()
        );
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
