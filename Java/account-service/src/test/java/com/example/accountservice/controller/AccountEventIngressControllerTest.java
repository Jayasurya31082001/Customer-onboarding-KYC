package com.example.accountservice.controller;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.verify;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.example.accountservice.model.Disposition;
import com.example.accountservice.service.AccountEventIngressService;

@ExtendWith(MockitoExtension.class)
class AccountEventIngressControllerTest {

    @Mock
    private AccountEventIngressService accountEventIngressService;

    private AccountEventIngressController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountEventIngressController(accountEventIngressService);
    }

    @Test
    void onRiskAssessed_delegatesAndReturnsAccepted() {
        RiskAssessedIngressRequest request = new RiskAssessedIngressRequest(
                "cust-1",
                "cust1@bank.test",
                Disposition.AUTO_APPROVE,
                81);

        var response = controller.onRiskAssessed(request);

        verify(accountEventIngressService).onRiskAssessed(
                "cust-1",
                "cust1@bank.test",
                Disposition.AUTO_APPROVE,
                81);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}
