package com.example.customerservice.customer.dto;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerEmailResponseTest {

    @Test
    void recordStoresEmail() {
        CustomerEmailResponse response = new CustomerEmailResponse("cust1@bank.test");

        assertThat(response.email()).isEqualTo("cust1@bank.test");
    }
}
