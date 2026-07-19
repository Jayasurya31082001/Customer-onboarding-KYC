package com.example.accountservice.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.accountservice.customer.client.CustomerServiceClient;
import com.example.accountservice.notification.client.NotificationEventPublisherClient;

@ExtendWith(MockitoExtension.class)
class AccountCreatedEventForwarderTest {

    @Mock
    private NotificationEventPublisherClient notificationEventPublisherClient;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @Test
    void shouldUpdateCustomerStatusAndPublishAccountCreatedNotification() {
        AccountCreatedEventForwarder forwarder = new AccountCreatedEventForwarder(
                notificationEventPublisherClient,
                customerServiceClient
        );

        AccountCreatedEvent event = new AccountCreatedEvent(
                this,
                "cust-1",
                "customer@example.com",
                "12345678",
                "102030"
        );

        forwarder.onAccountCreated(event);

        InOrder inOrder = inOrder(customerServiceClient, notificationEventPublisherClient);
        inOrder.verify(customerServiceClient, times(1)).updateOnboardingStatusToApproved("cust-1");
        inOrder.verify(notificationEventPublisherClient, times(1)).publishAccountCreated(org.mockito.ArgumentMatchers.any());
        inOrder.verifyNoMoreInteractions();
    }
}