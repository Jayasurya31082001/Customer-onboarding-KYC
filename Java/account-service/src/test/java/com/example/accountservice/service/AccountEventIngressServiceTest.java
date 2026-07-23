package com.example.accountservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.example.accountservice.events.RiskAssessedEvent;
import com.example.accountservice.model.Disposition;

@ExtendWith(MockitoExtension.class)
class AccountEventIngressServiceTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private AccountEventIngressService service;

    @BeforeEach
    void setUp() {
        service = new AccountEventIngressService(applicationEventPublisher);
    }

    @Test
    void onRiskAssessed_publishesRiskAssessedEvent() {
        service.onRiskAssessed("cust-1", "cust1@bank.test", Disposition.AUTO_APPROVE, 88);

        ArgumentCaptor<RiskAssessedEvent> eventCaptor = ArgumentCaptor.forClass(RiskAssessedEvent.class);
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        RiskAssessedEvent event = eventCaptor.getValue();
        assertThat(event.getSource()).isEqualTo(service);
        assertThat(event.getCustomerId()).isEqualTo("cust-1");
        assertThat(event.getCustomerEmail()).isEqualTo("cust1@bank.test");
        assertThat(event.getDisposition()).isEqualTo(Disposition.AUTO_APPROVE);
        assertThat(event.getScore()).isEqualTo(88);
    }
}
