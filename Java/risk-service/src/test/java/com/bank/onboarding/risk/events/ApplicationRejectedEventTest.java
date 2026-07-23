package com.bank.onboarding.risk.events;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ApplicationRejectedEventTest {

    @Test
    void constructor_setsAllFields() {
        Object source = new Object();

        ApplicationRejectedEvent event = new ApplicationRejectedEvent(
                source,
                "cust-1",
                "cust1@bank.test",
                "Sanctions match");

        assertThat(event.getSource()).isEqualTo(source);
        assertThat(event.getCustomerId()).isEqualTo("cust-1");
        assertThat(event.getCustomerEmail()).isEqualTo("cust1@bank.test");
        assertThat(event.getReason()).isEqualTo("Sanctions match");
    }
}
