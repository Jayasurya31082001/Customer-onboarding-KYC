package com.example.notificationservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.example.notificationservice.service.NotificationEventIngressService;

@ExtendWith(MockitoExtension.class)
class NotificationEventIngressControllerTest {

    @Mock
    private NotificationEventIngressService ingressService;

    private NotificationEventIngressController controller;

    @BeforeEach
    void setUp() {
        controller = new NotificationEventIngressController(ingressService);
    }

    @Test
    void onAccountCreated_delegatesAndReturnsAccepted() {
        AccountCreatedIngressRequest request = new AccountCreatedIngressRequest(
                "cust-1",
                "cust1@bank.test",
                "12345678",
                "110011");

        var response = controller.onAccountCreated(request);

        verify(ingressService).onAccountCreated("cust-1", "cust1@bank.test", "12345678", "110011");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void onApplicationRejected_delegatesAndReturnsAccepted() {
        ApplicationRejectedIngressRequest request = new ApplicationRejectedIngressRequest(
                "cust-1",
                "cust1@bank.test",
                "High risk");

        var response = controller.onApplicationRejected(request);

        verify(ingressService).onApplicationRejected("cust-1", "cust1@bank.test", "High risk");
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}
