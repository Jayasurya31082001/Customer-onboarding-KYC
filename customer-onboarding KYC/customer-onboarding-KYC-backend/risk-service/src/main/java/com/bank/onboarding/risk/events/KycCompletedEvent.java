package com.bank.onboarding.risk.events;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.springframework.context.ApplicationEvent;

import com.bank.onboarding.risk.model.KycStatus;

public class KycCompletedEvent extends ApplicationEvent {

    private final UUID kycCaseId;
    private final UUID customerId;
    private final UUID documentId;
    private final KycStatus status;
    private final LocalDateTime occurredAt;
    private final String correlationId;
    private final String nationality;
    private final LocalDate dateOfBirth;
    private final boolean pepMatch;
    private final boolean sanctionsMatch;
    private final String customerEmail;

    public KycCompletedEvent(Object source,
                             UUID kycCaseId,
                             UUID customerId,
                             UUID documentId,
                             KycStatus status,
                             LocalDateTime occurredAt,
                             String correlationId,
                             String nationality,
                             LocalDate dateOfBirth,
                             boolean pepMatch,
                             boolean sanctionsMatch,
                             String customerEmail) {
        super(source);
        this.kycCaseId = kycCaseId;
        this.customerId = customerId;
        this.documentId = documentId;
        this.status = status;
        this.occurredAt = occurredAt;
        this.correlationId = correlationId;
        this.nationality = nationality;
        this.dateOfBirth = dateOfBirth;
        this.pepMatch = pepMatch;
        this.sanctionsMatch = sanctionsMatch;
        this.customerEmail = customerEmail;
    }

    public UUID getKycCaseId() {
        return kycCaseId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getDocumentId() {
        return documentId;
    }

    public KycStatus getStatus() {
        return status;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public String getNationality() {
        return nationality;
    }

    public LocalDate getDateOfBirth() {
        return dateOfBirth;
    }

    public boolean isPepMatch() {
        return pepMatch;
    }

    public boolean isSanctionsMatch() {
        return sanctionsMatch;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }
}