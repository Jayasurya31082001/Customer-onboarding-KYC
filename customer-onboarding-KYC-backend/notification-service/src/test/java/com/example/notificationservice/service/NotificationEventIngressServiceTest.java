package com.example.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.notificationservice.events.AccountCreatedEvent;
import com.example.notificationservice.events.ApplicationRejectedEvent;

@ExtendWith(MockitoExtension.class)
class NotificationEventIngressServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private NotificationEventIngressService ingressService;

    @BeforeEach
    void setUp() {
        ingressService = new NotificationEventIngressService(applicationEventPublisher);
    }

    @Test
    void onAccountCreated_publishesEvent() {
        ingressService.onAccountCreated("cust-1", "cust1@bank.test", "12345678", "110011");

        ArgumentCaptor<AccountCreatedEvent> captor = ArgumentCaptor.forClass(AccountCreatedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        AccountCreatedEvent event = captor.getValue();
        assertThat(event.getCustomerId()).isEqualTo("cust-1");
        assertThat(event.getCustomerEmail()).isEqualTo("cust1@bank.test");
        assertThat(event.getAccountNumber()).isEqualTo("12345678");
        assertThat(event.getSortCode()).isEqualTo("110011");
    }

    @Test
    void onApplicationRejected_publishesEvent() {
        ingressService.onApplicationRejected("cust-1", "cust1@bank.test", "High risk");

        ArgumentCaptor<ApplicationRejectedEvent> captor = ArgumentCaptor.forClass(ApplicationRejectedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());

        ApplicationRejectedEvent event = captor.getValue();
        assertThat(event.getCustomerId()).isEqualTo("cust-1");
        assertThat(event.getCustomerEmail()).isEqualTo("cust1@bank.test");
        assertThat(event.getReason()).isEqualTo("High risk");
    }
}
