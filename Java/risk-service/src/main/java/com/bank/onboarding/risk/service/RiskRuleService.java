package com.bank.onboarding.risk.service;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.bank.onboarding.risk.cache.CacheConfig;
import com.bank.onboarding.risk.model.RiskRule;
import com.bank.onboarding.risk.repository.RiskRuleRepository;

@Service
public class RiskRuleService {

    private final RiskRuleRepository riskRuleRepository;

    public RiskRuleService(RiskRuleRepository riskRuleRepository) {
        this.riskRuleRepository = riskRuleRepository;
    }

    @Cacheable(value = CacheConfig.RISK_RULES_CACHE)
    public List<RiskRule> getActiveRiskRules() {
        return riskRuleRepository.findByActiveTrue();
    }
}