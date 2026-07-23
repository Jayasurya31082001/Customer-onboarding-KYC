package com.example.accountservice.corebanking.client;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class MockCoreBankingClientTest {

    @Test
    void provisionAccount_returnsDefaultSortCode() {
        MockCoreBankingClient client = new MockCoreBankingClient("110011");

        CoreBankingProvisionResponse response = client.provisionAccount("cust-1", "corr-1");

        assertThat(response.sortCode()).isEqualTo("110011");
    }
}
