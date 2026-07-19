package com.bank.onboarding.risk.model;

import org.hibernate.annotations.UuidGenerator;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "risk_rules")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RiskRule {

    @Id
    @UuidGenerator
    @Column(name = "rule_id", nullable = false, length = 36, updatable = false)
    private String ruleId;

    @Column(name = "rule_name", nullable = false, length = 100)
    private String ruleName;

    @Column(name = "risk_points", nullable = false)
    private int riskPoints;

    @Column(name = "active", nullable = false)
    private boolean active;
}