package com.bank.onboarding.risk.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;

import com.bank.onboarding.risk.client.AccountServiceClient;
import com.bank.onboarding.risk.client.CustomerServiceClient;
import com.bank.onboarding.risk.client.NotificationServiceClient;
import com.bank.onboarding.risk.model.Disposition;

@ExtendWith(MockitoExtension.class)
class RiskAssessmentEventForwarderTest {

    @Mock
    private AccountServiceClient accountServiceClient;

    @Mock
    private CustomerServiceClient customerServiceClient;

    @Mock
    private NotificationServiceClient notificationServiceClient;

    @Test
    void shouldForwardAutoApproveToAccountService() {
        RiskAssessmentEventForwarder forwarder = new RiskAssessmentEventForwarder(
                accountServiceClient,
                customerServiceClient,
                notificationServiceClient
        );

        RiskAssessedEvent event = new RiskAssessedEvent(this, "cust-1", "test@example.com", Disposition.AUTO_APPROVE, 12);

        forwarder.onRiskAssessed(event);

        verify(accountServiceClient, times(1)).publishRiskAssessed("cust-1", "test@example.com", Disposition.AUTO_APPROVE, 12);
    }

    @Test
    void shouldNotForwardNonAutoApproveToAccountService() {
        RiskAssessmentEventForwarder forwarder = new RiskAssessmentEventForwarder(
                accountServiceClient,
                customerServiceClient,
                notificationServiceClient
        );

        RiskAssessedEvent event = new RiskAssessedEvent(this, "cust-1", "test@example.com", Disposition.MANUAL_REVIEW, 55);

        forwarder.onRiskAssessed(event);

        verify(accountServiceClient, never()).publishRiskAssessed(org.mockito.ArgumentMatchers.anyString(),
            org.mockito.ArgumentMatchers.anyString(),
                org.mockito.ArgumentMatchers.any(),
                org.mockito.ArgumentMatchers.anyInt());
    }
}