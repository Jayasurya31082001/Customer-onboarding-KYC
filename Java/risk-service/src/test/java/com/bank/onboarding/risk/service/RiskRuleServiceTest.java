package com.bank.onboarding.risk.service;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.onboarding.risk.model.RiskRule;
import com.bank.onboarding.risk.repository.RiskRuleRepository;

@ExtendWith(MockitoExtension.class)
class RiskRuleServiceTest {

    @Mock
    private RiskRuleRepository riskRuleRepository;

    private RiskRuleService riskRuleService;

    @BeforeEach
    void setUp() {
        riskRuleService = new RiskRuleService(riskRuleRepository);
    }

    @Test
    void getActiveRiskRules_returnsRepositoryResult() {
        List<RiskRule> expected = List.of(
                RiskRule.builder().ruleId("r1").ruleName("High risk nationality").riskPoints(30).active(true).build(),
                RiskRule.builder().ruleId("r2").ruleName("PEP match").riskPoints(50).active(true).build());
        when(riskRuleRepository.findByActiveTrue()).thenReturn(expected);

        List<RiskRule> actual = riskRuleService.getActiveRiskRules();

        assertThat(actual).isEqualTo(expected);
    }
}
