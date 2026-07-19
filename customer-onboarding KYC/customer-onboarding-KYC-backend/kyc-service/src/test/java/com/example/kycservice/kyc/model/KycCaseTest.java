package com.example.kycservice.kyc.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class KycCaseTest {

    @Test
    void prePersist_setsDefaultsWhenMissing() {
        KycCase kycCase = new KycCase();

        kycCase.prePersist();

        assertThat(kycCase.getKycCaseId()).isNotNull();
        assertThat(kycCase.getStatus()).isEqualTo(KycStatus.KYC_IN_PROGRESS);
        assertThat(kycCase.getCreatedAt()).isNotNull();
        assertThat(kycCase.getUpdatedAt()).isEqualTo(kycCase.getCreatedAt());
    }

    @Test
    void prePersist_preservesProvidedValues() {
        KycCase kycCase = new KycCase();
        UUID id = UUID.randomUUID();
        LocalDateTime created = LocalDateTime.now().minusDays(1);
        LocalDateTime updated = LocalDateTime.now().minusHours(1);
        kycCase.setKycCaseId(id);
        kycCase.setStatus(KycStatus.PASS);
        kycCase.setCreatedAt(created);
        kycCase.setUpdatedAt(updated);

        kycCase.prePersist();

        assertThat(kycCase.getKycCaseId()).isEqualTo(id);
        assertThat(kycCase.getStatus()).isEqualTo(KycStatus.PASS);
        assertThat(kycCase.getCreatedAt()).isEqualTo(created);
        assertThat(kycCase.getUpdatedAt()).isEqualTo(updated);
    }

    @Test
    void preUpdate_refreshesUpdatedAt() {
        KycCase kycCase = new KycCase();
        kycCase.setUpdatedAt(LocalDateTime.now().minusDays(1));

        kycCase.preUpdate();

        assertThat(kycCase.getUpdatedAt()).isNotNull();
        assertThat(kycCase.getUpdatedAt()).isAfter(LocalDateTime.now().minusMinutes(1));
    }

    @Test
    void gettersAndSetters_coverFields() {
        KycCase kycCase = new KycCase();
        UUID caseId = UUID.randomUUID();
        UUID customerId = UUID.randomUUID();
        UUID documentId = UUID.randomUUID();
        LocalDate dob = LocalDate.of(1990, 1, 1);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(2);
        LocalDateTime updatedAt = LocalDateTime.now().minusDays(1);

        kycCase.setKycCaseId(caseId);
        kycCase.setCustomerId(customerId);
        kycCase.setDocumentId(documentId);
        kycCase.setIdempotencyKey("idem-1");
        kycCase.setProvidedFirstName("Jane");
        kycCase.setProvidedLastName("Doe");
        kycCase.setProvidedDateOfBirth(dob);
        kycCase.setStatus(KycStatus.FAIL);
        kycCase.setNotes("note");
        kycCase.setCreatedAt(createdAt);
        kycCase.setUpdatedAt(updatedAt);

        assertThat(kycCase.getKycCaseId()).isEqualTo(caseId);
        assertThat(kycCase.getCustomerId()).isEqualTo(customerId);
        assertThat(kycCase.getDocumentId()).isEqualTo(documentId);
        assertThat(kycCase.getIdempotencyKey()).isEqualTo("idem-1");
        assertThat(kycCase.getProvidedFirstName()).isEqualTo("Jane");
        assertThat(kycCase.getProvidedLastName()).isEqualTo("Doe");
        assertThat(kycCase.getProvidedDateOfBirth()).isEqualTo(dob);
        assertThat(kycCase.getStatus()).isEqualTo(KycStatus.FAIL);
        assertThat(kycCase.getNotes()).isEqualTo("note");
        assertThat(kycCase.getCreatedAt()).isEqualTo(createdAt);
        assertThat(kycCase.getUpdatedAt()).isEqualTo(updatedAt);
    }
}
