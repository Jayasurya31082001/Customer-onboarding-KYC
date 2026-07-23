package com.example.kycservice.kyc.dto;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.example.kycservice.customer.client.CustomerProfile;
import com.example.kycservice.document.client.DocumentDetails;

class IngressAndRecordDtosTest {

    @Test
    void customerRegisteredIngressRequest_storesFields() {
        UUID customerId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();

        CustomerRegisteredIngressRequest request = new CustomerRegisteredIngressRequest(customerId, occurredAt, "corr-1");

        assertThat(request.customerId()).isEqualTo(customerId);
        assertThat(request.occurredAt()).isEqualTo(occurredAt);
        assertThat(request.correlationId()).isEqualTo("corr-1");
    }

    @Test
    void documentUploadedIngressRequest_storesFields() {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.now();

        DocumentUploadedIngressRequest request = new DocumentUploadedIngressRequest(documentId, customerId, occurredAt, "corr-2");

        assertThat(request.documentId()).isEqualTo(documentId);
        assertThat(request.customerId()).isEqualTo(customerId);
        assertThat(request.occurredAt()).isEqualTo(occurredAt);
    }

    @Test
    void customerProfile_recordStoresFields() {
        UUID customerId = UUID.randomUUID();
        LocalDate dob = LocalDate.of(1992, 4, 12);

        CustomerProfile profile = new CustomerProfile(customerId, "Jane", "Doe", dob, "US", "ONBOARDED", "jane@example.com");

        assertThat(profile.customerId()).isEqualTo(customerId);
        assertThat(profile.firstName()).isEqualTo("Jane");
        assertThat(profile.email()).isEqualTo("jane@example.com");
    }

    @Test
    void documentDetails_recordStoresFields() {
        UUID documentId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();

        DocumentDetails details = new DocumentDetails(documentId, customerId, "passport.pdf", "application/pdf", 123L);

        assertThat(details.documentId()).isEqualTo(documentId);
        assertThat(details.customerId()).isEqualTo(customerId);
        assertThat(details.fileName()).isEqualTo("passport.pdf");
    }
}
