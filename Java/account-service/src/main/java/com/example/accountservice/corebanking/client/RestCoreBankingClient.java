package com.example.accountservice.corebanking.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;

@Component
@ConditionalOnProperty(name = "core-banking.mock-enabled", havingValue = "false", matchIfMissing = true)
public class RestCoreBankingClient implements CoreBankingClient {

    private final RestClient.Builder restClientBuilder;
    private final String coreBankingBaseUrl;

    public RestCoreBankingClient(RestClient.Builder restClientBuilder,
                                 @Value("${core-banking.base-url}") String coreBankingBaseUrl) {
        this.restClientBuilder = restClientBuilder;
        this.coreBankingBaseUrl = coreBankingBaseUrl;
    }

    @Override
    @CircuitBreaker(name = "coreBanking", fallbackMethod = "fallbackProvision")
    @Retry(name = "coreBanking")
    public CoreBankingProvisionResponse provisionAccount(String customerId, String correlationId) {
        RestClient client = restClientBuilder.baseUrl(coreBankingBaseUrl).build();
        return client.post()
                .uri("/api/v1/core/accounts/provision")
                .contentType(MediaType.APPLICATION_JSON)
                .header("X-Correlation-ID", correlationId == null ? "" : correlationId)
                .body(new CoreBankingProvisionRequest(customerId))
                .retrieve()
                .body(CoreBankingProvisionResponse.class);
    }

    public CoreBankingProvisionResponse fallbackProvision(String customerId, String correlationId, Exception exception) {
        throw new CoreBankingUnavailableException(
                "Core banking provisioning failed for customerId=" + customerId,
                exception);
    }

    private record CoreBankingProvisionRequest(String customerId) {
    }
}
