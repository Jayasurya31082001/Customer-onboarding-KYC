package com.example.notificationservice.provider.client;

import org.junit.jupiter.api.Test;

class MockNotificationSenderClientTest {

    @Test
    void send_doesNotThrow() {
        MockNotificationSenderClient client = new MockNotificationSenderClient();

        client.send("cust1@bank.test", "Message", "corr-1");
    }
}
