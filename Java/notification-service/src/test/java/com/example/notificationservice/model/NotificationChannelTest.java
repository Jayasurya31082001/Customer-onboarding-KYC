package com.example.notificationservice.model;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class NotificationChannelTest {

    @Test
    void enumContainsExpectedValues() {
        assertThat(NotificationChannel.valueOf("EMAIL")).isEqualTo(NotificationChannel.EMAIL);
        assertThat(NotificationChannel.valueOf("SMS")).isEqualTo(NotificationChannel.SMS);
    }
}
