package com.example.notificationservice.events;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class ApplicationRejectedEventTest {

    @Test
    void constructor_setsAllFields() {
        Object source = new Object();

        ApplicationRejectedEvent event = new ApplicationRejectedEvent(source, "cust-1", "cust1@bank.test", "High risk");

        assertThat(event.getSource()).isEqualTo(source);
        assertThat(event.getCustomerId()).isEqualTo("cust-1");
        assertThat(event.getCustomerEmail()).isEqualTo("cust1@bank.test");
        assertThat(event.getReason()).isEqualTo("High risk");
    }
}
