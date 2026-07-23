package com.bank.onboarding.risk.events;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.Test;

class ManualReviewRequiredEventTest {

    @Test
    void constructor_setsAllFields() {
        Object source = new Object();

        ManualReviewRequiredEvent event = new ManualReviewRequiredEvent(
                source,
                "cust-1",
                "cust1@bank.test",
                67,
                "High risk profile");

        assertThat(event.getSource()).isEqualTo(source);
        assertThat(event.getCustomerId()).isEqualTo("cust-1");
        assertThat(event.getCustomerEmail()).isEqualTo("cust1@bank.test");
        assertThat(event.getRiskScore()).isEqualTo(67);
        assertThat(event.getReason()).isEqualTo("High risk profile");
    }
}
