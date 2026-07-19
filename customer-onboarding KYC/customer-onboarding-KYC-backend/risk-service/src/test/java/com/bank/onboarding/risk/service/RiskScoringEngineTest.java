package com.bank.onboarding.risk.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.bank.onboarding.risk.events.KycCompletedEvent;
import com.bank.onboarding.risk.model.Disposition;
import com.bank.onboarding.risk.model.KycStatus;
import com.bank.onboarding.risk.model.RiskRule;

class RiskScoringEngineTest {

    private RiskScoringEngine riskScoringEngine;
    private List<RiskRule> rules;

    @BeforeEach
    void setUp() {
        RiskProperties riskProperties = new RiskProperties();
        riskProperties.setHighRiskNationalities(Set.of("IR", "KP"));
        Clock fixedClock = Clock.fixed(Instant.parse("2026-07-13T00:00:00Z"), ZoneOffset.UTC);
        riskScoringEngine = new RiskScoringEngine(riskProperties, fixedClock);
        rules = List.of(
                rule("KYC_FAIL", 80),
                rule("KYC_REFER", 40),
                rule("HIGH_RISK_NATIONALITY", 30),
                rule("AGE_UNDER_25", 10),
                rule("PEP_MATCH", 50),
                rule("SANCTIONS_MATCH", 100)
        );
    }

    @Test
    void shouldMapBoundaryScoresToExpectedDispositions() {
        assertThat(riskScoringEngine.scoreToDisposition(30)).isEqualTo(Disposition.AUTO_APPROVE);
        assertThat(riskScoringEngine.scoreToDisposition(31)).isEqualTo(Disposition.MANUAL_REVIEW);
        assertThat(riskScoringEngine.scoreToDisposition(70)).isEqualTo(Disposition.MANUAL_REVIEW);
        assertThat(riskScoringEngine.scoreToDisposition(71)).isEqualTo(Disposition.AUTO_REJECT);
    }

    @Test
    void shouldRejectWhenKycFailsAlone() {
        int score = riskScoringEngine.calculate(event(KycStatus.FAIL, null, null, false, false), rules);

        assertThat(score).isEqualTo(80);
        assertThat(riskScoringEngine.scoreToDisposition(score)).isEqualTo(Disposition.AUTO_REJECT);
    }

    @Test
    void shouldRejectWhenSanctionsMatchIsPresent() {
        int score = riskScoringEngine.calculate(event(KycStatus.PASS, null, null, false, true), rules);

        assertThat(score).isEqualTo(100);
        assertThat(riskScoringEngine.scoreToDisposition(score)).isEqualTo(Disposition.AUTO_REJECT);
    }

    @Test
    void shouldApplyAgeUnderTwentyFiveRuleOnlyWhenCustomerIsYoungEnough() {
        int youngerScore = riskScoringEngine.calculate(
                event(KycStatus.PASS, "US", LocalDate.of(2005, 7, 14), false, false),
                rules
        );
        int olderScore = riskScoringEngine.calculate(
                event(KycStatus.PASS, "US", LocalDate.of(1990, 7, 13), false, false),
                rules
        );

        assertThat(youngerScore).isEqualTo(10);
        assertThat(olderScore).isZero();
    }

    @Test
    void shouldHandleNullKycResultSafely() {
        int score = riskScoringEngine.calculate(event(null, null, null, false, false), rules);

        assertThat(score).isZero();
        assertThat(riskScoringEngine.scoreToDisposition(score)).isEqualTo(Disposition.AUTO_APPROVE);
    }

    private RiskRule rule(String name, int points) {
        return RiskRule.builder()
                .ruleName(name)
                .riskPoints(points)
                .active(true)
                .build();
    }

    private KycCompletedEvent event(KycStatus status,
                                    String nationality,
                                    LocalDate dateOfBirth,
                                    boolean pepMatch,
                                    boolean sanctionsMatch) {
        return new KycCompletedEvent(
                this,
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                status,
                LocalDateTime.of(2026, 7, 13, 0, 0),
                "corr-123",
                nationality,
                dateOfBirth,
                pepMatch,
                sanctionsMatch,
                "test@example.com"
        );
    }
}