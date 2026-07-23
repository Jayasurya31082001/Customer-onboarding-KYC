package com.example.notificationservice.exception;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationDeliveryExceptionTest {

    @Test
    void constructor_setsMessageAndCause() {
        RuntimeException cause = new RuntimeException("root cause");

        NotificationDeliveryException exception = new NotificationDeliveryException("delivery failed", cause);

        assertThat(exception).hasMessage("delivery failed");
        assertThat(exception.getCause()).isEqualTo(cause);
    }
}
