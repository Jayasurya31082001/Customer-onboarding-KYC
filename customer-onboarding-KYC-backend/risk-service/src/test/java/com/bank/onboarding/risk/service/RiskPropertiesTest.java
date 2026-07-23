package com.bank.onboarding.risk.service;

import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class RiskPropertiesTest {

    @Test
    void highRiskNationalities_getterAndSetterWork() {
        RiskProperties properties = new RiskProperties();
        Set<String> configured = new LinkedHashSet<>(Set.of("IR", "KP"));

        properties.setHighRiskNationalities(configured);

        assertThat(properties.getHighRiskNationalities()).containsExactlyInAnyOrder("IR", "KP");
    }
}
