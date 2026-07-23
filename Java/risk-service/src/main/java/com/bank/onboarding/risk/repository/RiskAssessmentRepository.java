package com.bank.onboarding.risk.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.onboarding.risk.model.RiskAssessment;

public interface RiskAssessmentRepository extends JpaRepository<RiskAssessment, String> {

	Optional<RiskAssessment> findTopByCustomerIdOrderByAssessedAtDesc(String customerId);

	/** Incremental sync query — returns assessments assessed after the cursor. */
	Page<RiskAssessment> findByAssessedAtAfter(LocalDateTime assessedAfter, Pageable pageable);
}