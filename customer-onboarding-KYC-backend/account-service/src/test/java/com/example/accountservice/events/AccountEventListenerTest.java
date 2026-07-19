package com.example.accountservice.events;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEvent;
import org.springframework.context.ApplicationEventPublisher;

import com.example.accountservice.model.Account;
import com.example.accountservice.model.Disposition;
import com.example.accountservice.service.AccountProvisioningService;

@ExtendWith(MockitoExtension.class)
class AccountEventListenerTest {

    @Mock
    private AccountProvisioningService accountCreationService;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @Captor
    private ArgumentCaptor<ApplicationEvent> eventCaptor;

    @Test
    void shouldCreateAccountAndPublishEventForAutoApprove() {
        AccountEventListener listener = new AccountEventListener(accountCreationService, applicationEventPublisher);
        RiskAssessedEvent event = new RiskAssessedEvent(this, "cust-1", "test@example.com", Disposition.AUTO_APPROVE, 20);

        when(accountCreationService.createAccountForCustomer("cust-1", null)).thenReturn(Account.builder()
                .accountId("acc-1")
                .customerId("cust-1")
                .accountNumber("12345678")
                .sortCode("102030")
                .build());

        listener.handleRiskAssessed(event);

        verify(accountCreationService, times(1)).createAccountForCustomer("cust-1", null);
        verify(applicationEventPublisher, times(1)).publishEvent(eventCaptor.capture());
    }

    @Test
    void shouldSkipAccountCreationForNonApprovedDisposition() {
            AccountEventListener listener = new AccountEventListener(accountCreationService, applicationEventPublisher);
            RiskAssessedEvent event = new RiskAssessedEvent(this, "cust-1", "test@example.com", Disposition.MANUAL_REVIEW, 55);

        listener.handleRiskAssessed(event);

        verify(accountCreationService, never()).createAccountForCustomer("cust-1", null);
        verify(applicationEventPublisher, never()).publishEvent(org.mockito.ArgumentMatchers.any());
    }
}
