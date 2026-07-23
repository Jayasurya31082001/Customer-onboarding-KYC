package com.bank.onboarding.risk.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.bank.onboarding.risk.model.Disposition;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class AccountServiceClient {

    private final RestClient.Builder restClientBuilder;
    private final String accountServiceBaseUrl;

    public AccountServiceClient(RestClient.Builder restClientBuilder,
                                @Value("${account.service.base-url}") String accountServiceBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.accountServiceBaseUrl = accountServiceBaseUrl;
    }

    public void publishRiskAssessed(String customerId, String customerEmail, Disposition disposition, int score) {
        try {
            RestClient client = restClientBuilder.baseUrl(accountServiceBaseUrl).build();
            client.post()
                    .uri("/api/internal/events/risk-assessed")
                    .body(new RiskAssessedIngressRequest(customerId, customerEmail, disposition, score))
                    .retrieve()
                    .toBodilessEntity();
            log.info("Forwarded risk-assessed event to account-service. customerId={}, disposition={}, score={}",
                    customerId, disposition, score);
        } catch (Exception e) {
            log.error("Failed to forward risk-assessed event to account-service. customerId={}", customerId, e);
            throw e;
        }
    }

    private record RiskAssessedIngressRequest(String customerId, String customerEmail, Disposition disposition, int score) {}
}