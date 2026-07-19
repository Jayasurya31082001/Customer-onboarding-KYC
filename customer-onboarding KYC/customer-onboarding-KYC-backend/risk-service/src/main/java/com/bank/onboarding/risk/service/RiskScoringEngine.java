package com.bank.onboarding.risk.service;

import java.time.Clock;
import java.time.LocalDate;
import java.time.Period;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.bank.onboarding.risk.events.KycCompletedEvent;
import com.bank.onboarding.risk.model.Disposition;
import com.bank.onboarding.risk.model.KycStatus;
import com.bank.onboarding.risk.model.RiskRule;

@Component
public class RiskScoringEngine {

    private static final String RULE_KYC_FAIL = "KYC_FAIL";
    private static final String RULE_KYC_REFER = "KYC_REFER";
    private static final String RULE_HIGH_RISK_NATIONALITY = "HIGH_RISK_NATIONALITY";
    private static final String RULE_AGE_UNDER_25 = "AGE_UNDER_25";
    private static final String RULE_PEP_MATCH = "PEP_MATCH";
    private static final String RULE_SANCTIONS_MATCH = "SANCTIONS_MATCH";

    private final RiskProperties riskProperties;
    private final Clock clock;

    @Autowired
    public RiskScoringEngine(RiskProperties riskProperties) {
        this(riskProperties, Clock.systemDefaultZone());
    }

    RiskScoringEngine(RiskProperties riskProperties, Clock clock) {
        this.riskProperties = riskProperties;
        this.clock = clock;
    }

    public int calculate(KycCompletedEvent event, List<RiskRule> rules) {
        int score = 0;
        score += scoreForKycStatus(event.getStatus(), rules);
        score += scoreForNationality(event.getNationality(), rules);
        score += scoreForAge(event.getDateOfBirth(), rules);
        score += event.isPepMatch() ? rulePoints(RULE_PEP_MATCH, rules, 50) : 0;
        score += event.isSanctionsMatch() ? rulePoints(RULE_SANCTIONS_MATCH, rules, 100) : 0;
        return score;
    }

    public Disposition scoreToDisposition(int score) {
        if (score <= 30) {
            return Disposition.AUTO_APPROVE;
        }

        if (score <= 70) {
            return Disposition.MANUAL_REVIEW;
        }

        return Disposition.AUTO_REJECT;
    }

    private int scoreForKycStatus(KycStatus status, List<RiskRule> rules) {
        if (status == null) {
            return 0;
        }

        if (status == KycStatus.FAIL) {
            return rulePoints(RULE_KYC_FAIL, rules, 80);
        }

        if (status == KycStatus.REFER) {
            return rulePoints(RULE_KYC_REFER, rules, 40);
        }

        return 0;
    }

    private int scoreForNationality(String nationality, List<RiskRule> rules) {
        if (nationality == null || nationality.isBlank()) {
            return 0;
        }

        Set<String> watchlist = riskProperties.getHighRiskNationalities().stream()
                .map(value -> value.toUpperCase(Locale.ROOT))
                .collect(java.util.stream.Collectors.toSet());

        return watchlist.contains(nationality.toUpperCase(Locale.ROOT))
                ? rulePoints(RULE_HIGH_RISK_NATIONALITY, rules, 30)
                : 0;
    }

    private int scoreForAge(LocalDate dateOfBirth, List<RiskRule> rules) {
        if (dateOfBirth == null) {
            return 0;
        }

        return Period.between(dateOfBirth, LocalDate.now(clock)).getYears() < 25
                ? rulePoints(RULE_AGE_UNDER_25, rules, 10)
                : 0;
    }

    private int rulePoints(String ruleName, List<RiskRule> rules, int defaultPoints) {
        return rules.stream()
                .filter(rule -> ruleName.equalsIgnoreCase(rule.getRuleName()))
                .map(RiskRule::getRiskPoints)
                .findFirst()
                .orElse(defaultPoints);
    }
}