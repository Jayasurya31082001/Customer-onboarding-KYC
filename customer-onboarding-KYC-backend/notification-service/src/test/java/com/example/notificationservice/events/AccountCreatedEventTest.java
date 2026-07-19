package com.example.notificationservice.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AccountCreatedEventTest {

    @Test
    void constructor_setsAllFields() {
        Object source = new Object();

        AccountCreatedEvent event = new AccountCreatedEvent(source, "cust-1", "cust1@bank.test", "12345678", "110011");

        assertThat(event.getSource()).isEqualTo(source);
        assertThat(event.getCustomerId()).isEqualTo("cust-1");
        assertThat(event.getCustomerEmail()).isEqualTo("cust1@bank.test");
        assertThat(event.getAccountNumber()).isEqualTo("12345678");
        assertThat(event.getSortCode()).isEqualTo("110011");
    }
}
