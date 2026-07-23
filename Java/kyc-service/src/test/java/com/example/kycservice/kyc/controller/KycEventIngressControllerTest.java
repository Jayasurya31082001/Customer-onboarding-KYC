package com.example.kycservice.kyc.controller;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.example.kycservice.kyc.dto.CustomerRegisteredIngressRequest;
import com.example.kycservice.kyc.dto.DocumentUploadedIngressRequest;
import com.example.kycservice.kyc.service.KycEventOrchestrationService;

@ExtendWith(MockitoExtension.class)
class KycEventIngressControllerTest {

    @Mock
    private KycEventOrchestrationService orchestrationService;

    private KycEventIngressController controller;

    @BeforeEach
    void setUp() {
        controller = new KycEventIngressController(orchestrationService);
    }

    @Test
    void onCustomerRegistered_delegatesAndReturnsAccepted() {
        UUID customerId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        CustomerRegisteredIngressRequest request = new CustomerRegisteredIngressRequest(customerId, occurredAt, "corr-1");

        var response = controller.onCustomerRegistered(request);

        verify(orchestrationService).onCustomerRegistered(customerId, occurredAt, "corr-1");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void onDocumentUploaded_delegatesAndReturnsAccepted() {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();
        DocumentUploadedIngressRequest request = new DocumentUploadedIngressRequest(documentId, customerId, occurredAt, "corr-2");

        var response = controller.onDocumentUploaded(request);

        verify(orchestrationService).onDocumentUploaded(customerId, documentId, occurredAt, "corr-2");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}
