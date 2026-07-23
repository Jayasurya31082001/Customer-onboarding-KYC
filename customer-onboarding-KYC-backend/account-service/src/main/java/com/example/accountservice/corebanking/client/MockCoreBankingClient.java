package com.example.accountservice.corebanking.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(name = "core-banking.mock-enabled", havingValue = "true")
public class MockCoreBankingClient implements CoreBankingClient {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockCoreBankingClient.class);

    private final String defaultSortCode;

    public MockCoreBankingClient(@Value("${core-banking.default-sort-code}") String defaultSortCode) {
        this.defaultSortCode = defaultSortCode;
    }

    @Override
    public CoreBankingProvisionResponse provisionAccount(String customerId, String correlationId) {
        LOGGER.info("Mock core-banking enabled; provisioning account with default sort code. customerId={}, correlationId={}",
                customerId, correlationId);
        return new CoreBankingProvisionResponse(defaultSortCode);
    }
}
