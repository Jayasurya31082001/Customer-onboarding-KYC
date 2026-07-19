package com.bank.onboarding.risk.service;

import java.util.LinkedHashSet;
import java.util.Set;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "risk")
public class RiskProperties {

    private Set<String> highRiskNationalities = new LinkedHashSet<>();

    public Set<String> getHighRiskNationalities() {
        return highRiskNationalities;
    }

    public void setHighRiskNationalities(Set<String> highRiskNationalities) {
        this.highRiskNationalities = highRiskNationalities;
    }
}