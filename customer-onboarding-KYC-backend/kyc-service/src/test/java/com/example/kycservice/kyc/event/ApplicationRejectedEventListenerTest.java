package com.example.kycservice.kyc.event;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.kycservice.customer.client.CustomerClient;
import com.example.kycservice.notification.client.NotificationClient;

@ExtendWith(MockitoExtension.class)
class ApplicationRejectedEventListenerTest {

    private static final String REJECTED = "REJECTED";

    @Mock
    private CustomerClient customerClient;

    @Mock
    private NotificationClient notificationClient;

    private ApplicationRejectedEventListener listener;

    @BeforeEach
    void setUp() {
        listener = new ApplicationRejectedEventListener(customerClient, notificationClient);
    }

    @Test
    @DisplayName("onApplicationRejected forwards event to notification-service")
    void onApplicationRejected_forwardsNotification() {
        UUID customerId = UUID.randomUUID();
        ApplicationRejectedEvent event = new ApplicationRejectedEvent(
                customerId,
                "customer@example.com",
                "KYC failed due to name mismatch",
                "corr-1"
        );

        listener.onApplicationRejected(event);

        verify(customerClient).updateOnboardingStatus(customerId, REJECTED, "corr-1");
        verify(notificationClient).sendApplicationRejected(
                customerId,
                "customer@example.com",
                "KYC failed due to name mismatch",
                "corr-1"
        );
    }

    @Test
    @DisplayName("onApplicationRejected skips notification when email is missing")
    void onApplicationRejected_skipsWhenEmailMissing() {
        UUID customerId = UUID.randomUUID();
        ApplicationRejectedEvent event = new ApplicationRejectedEvent(
                customerId,
                "   ",
                "KYC failed",
                "corr-2"
        );

        listener.onApplicationRejected(event);

        verify(customerClient).updateOnboardingStatus(customerId, REJECTED, "corr-2");
        verify(notificationClient, never()).sendApplicationRejected(
                customerId,
                "   ",
                "KYC failed",
                "corr-2"
        );
    }
}

