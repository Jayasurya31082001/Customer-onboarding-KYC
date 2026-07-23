package com.example.kycservice.kyc.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "kyc_cases")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KycCase {

    @Id
    @Column(name = "kyc_case_id", nullable = false, updatable = false)
    private UUID kycCaseId;

    @Column(name = "customer_id", nullable = false, updatable = false)
    private UUID customerId;

    @Column(name = "document_id", nullable = false, updatable = false)
    private UUID documentId;

    @Column(name = "idempotency_key", nullable = false, updatable = false, unique = true, length = 128)
    private String idempotencyKey;

    @Column(name = "provided_first_name", nullable = false, length = 50)
    private String providedFirstName;

    @Column(name = "provided_last_name", nullable = false, length = 50)
    private String providedLastName;

    @Column(name = "provided_date_of_birth", nullable = false)
    private LocalDate providedDateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 30)
    private KycStatus status;

    @Column(name = "notes", nullable = false, length = 255)
    private String notes;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @PrePersist
    void prePersist() {
        if (kycCaseId == null) {
            kycCaseId = UUID.randomUUID();
        }
        if (status == null) {
            status = KycStatus.KYC_IN_PROGRESS;
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (updatedAt == null) {
            updatedAt = createdAt;
        }
    }

    @PreUpdate
    void preUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
