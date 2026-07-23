package com.bank.onboarding.risk.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.onboarding.risk.model.RiskAssessment;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, String> {

	Optional<RiskAssessment> findTopByCustomerIdOrderByAssessedAtDesc(String customerId);
}