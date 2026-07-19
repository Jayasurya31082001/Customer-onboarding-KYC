package com.bank.onboarding.risk.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.bank.onboarding.risk.model.RiskRule;

public interface RiskRuleRepository extends JpaRepository<RiskRule, String> {

    List<RiskRule> findByActiveTrue();
}