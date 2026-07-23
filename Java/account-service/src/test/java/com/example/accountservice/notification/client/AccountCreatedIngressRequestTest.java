package com.example.accountservice.notification.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.Test;

class AccountCreatedIngressRequestTest {

    @Test
    void constructor_acceptsValidValues() {
        AccountCreatedIngressRequest request = new AccountCreatedIngressRequest(
                "cust-1",
                "cust1@bank.test",
                "12345678",
                "110011");

        assertThat(request.customerId()).isEqualTo("cust-1");
        assertThat(request.customerEmail()).isEqualTo("cust1@bank.test");
        assertThat(request.accountNumber()).isEqualTo("12345678");
        assertThat(request.sortCode()).isEqualTo("110011");
    }

    @Test
    void constructor_rejectsBlankCustomerId() {
        assertThatThrownBy(() -> new AccountCreatedIngressRequest(" ", "cust1@bank.test", "12345678", "110011"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("customerId");
    }
}
