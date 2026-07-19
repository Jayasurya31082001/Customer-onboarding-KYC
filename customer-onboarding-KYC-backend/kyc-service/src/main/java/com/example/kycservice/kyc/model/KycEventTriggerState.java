package com.example.kycservice.kyc.model;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "kyc_event_trigger_state")
@Getter
@Setter
public class KycEventTriggerState {

    @Id
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "customer_registered_received", nullable = false)
    private boolean customerRegisteredReceived;

    @Column(name = "latest_document_id")
    private UUID latestDocumentId;

    @Column(name = "last_started_document_id")
    private UUID lastStartedDocumentId;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
