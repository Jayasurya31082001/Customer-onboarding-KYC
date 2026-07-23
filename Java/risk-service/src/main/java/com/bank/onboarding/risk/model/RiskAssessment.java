package com.bank.onboarding.risk.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "risk_assessments")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskAssessment {

    @Id
    @UuidGenerator
    @Column(name = "assessment_id", nullable = false, length = 36, updatable = false)
    private String assessmentId;

    @Column(name = "customer_id", nullable = false, length = 36)
    private String customerId;

    @Column(name = "score", nullable = false)
    private int score;

    @Enumerated(EnumType.STRING)
    @Column(name = "disposition", nullable = false, length = 32)
    private Disposition disposition;

    @CreatedDate
    @Column(name = "assessed_at", nullable = false, updatable = false)
    private LocalDateTime assessedAt;

    @LastModifiedDate
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}