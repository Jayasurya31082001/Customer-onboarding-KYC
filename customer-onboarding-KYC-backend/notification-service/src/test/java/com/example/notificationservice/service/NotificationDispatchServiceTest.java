package com.example.notificationservice.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.notificationservice.model.Template;
import com.example.notificationservice.provider.client.NotificationSenderClient;
import com.example.notificationservice.repository.NotificationLogRepository;

@ExtendWith(MockitoExtension.class)
class NotificationDispatchServiceTest {

    @Mock
    private NotificationTemplateService notificationTemplateService;

    @Mock
    private NotificationSenderClient notificationSenderClient;

    @Mock
    private NotificationLogRepository notificationLogRepository;

    private NotificationDispatchService dispatchService;

    @BeforeEach
    void setUp() {
        dispatchService = new NotificationDispatchService(
                notificationTemplateService,
                notificationSenderClient,
                notificationLogRepository);
    }

    @Test
    void sendAccountCreated_sendsNotificationAndPersistsSentLog() {
        Template template = Template.builder()
                .templateKey("ACCOUNT_CREATED")
                .content("Customer ${customerId}, account ${accountNumber} / ${sortCode}")
                .active(true)
                .build();
        when(notificationTemplateService.getActiveTemplate("ACCOUNT_CREATED")).thenReturn(template);

        dispatchService.sendAccountCreated("cust-1", "cust1@bank.test", "12345678", "110011", "corr-1");

        verify(notificationSenderClient).send("cust1@bank.test", "Customer cust-1, account 12345678 / 110011", "corr-1");
        verify(notificationLogRepository).save(any());
    }

    @Test
    void sendAccountCreated_marksFailedAndRethrowsOnSenderError() {
        Template template = Template.builder()
                .templateKey("ACCOUNT_CREATED")
                .content("Customer ${customerId}, account ${accountNumber} / ${sortCode}")
                .active(true)
                .build();
        when(notificationTemplateService.getActiveTemplate("ACCOUNT_CREATED")).thenReturn(template);

        org.mockito.Mockito.doThrow(new RuntimeException("send failed"))
                .when(notificationSenderClient)
                .send(any(), any(), any());

        assertThatThrownBy(() -> dispatchService.sendAccountCreated(
                "cust-1",
                "cust1@bank.test",
                "12345678",
                "110011",
                "corr-1"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("send failed");

        verify(notificationLogRepository).save(any());
    }

    @Test
    void sendApplicationRejected_sendsNotificationAndPersistsSentLog() {
        Template template = Template.builder()
                .templateKey("APPLICATION_REJECTED")
                .content("Customer ${customerId}, rejected: ${reason}")
                .active(true)
                .build();
        when(notificationTemplateService.getActiveTemplate("APPLICATION_REJECTED")).thenReturn(template);

        dispatchService.sendApplicationRejected("cust-1", "cust1@bank.test", "High risk", "corr-1");

        verify(notificationSenderClient).send("cust1@bank.test", "Customer cust-1, rejected: High risk", "corr-1");
        verify(notificationLogRepository).save(any());
    }
}
