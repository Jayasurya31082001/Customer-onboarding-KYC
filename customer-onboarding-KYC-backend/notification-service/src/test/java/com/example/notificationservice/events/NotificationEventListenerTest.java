package com.example.notificationservice.events;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.notificationservice.service.NotificationDispatchOperations;

@ExtendWith(MockitoExtension.class)
class NotificationEventListenerTest {

    @Mock
    private NotificationDispatchOperations notificationDispatchService;

    @Test
    void shouldProcessAccountCreatedEvent() {
        NotificationEventListener listener = new NotificationEventListener(notificationDispatchService);
        AccountCreatedEvent event = new AccountCreatedEvent(
            this, "cust-1", "cust-1@example.com", "12345678", "102030");

        listener.onAccountCreated(event);

        verify(notificationDispatchService, times(1))
            .sendAccountCreated("cust-1", "cust-1@example.com", "12345678", "102030", null);
    }

    @Test
    void shouldProcessApplicationRejectedEvent() {
        NotificationEventListener listener = new NotificationEventListener(notificationDispatchService);
        ApplicationRejectedEvent event = new ApplicationRejectedEvent(
            this, "cust-1", "cust-1@example.com", "risk score too high");

        listener.onApplicationRejected(event);

        verify(notificationDispatchService, times(1))
            .sendApplicationRejected("cust-1", "cust-1@example.com", "risk score too high", null);
    }
}
