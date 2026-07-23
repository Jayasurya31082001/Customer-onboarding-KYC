package com.example.accountservice.corebanking.client;

public interface CoreBankingClient {

    CoreBankingProvisionResponse provisionAccount(String customerId, String correlationId);
}
