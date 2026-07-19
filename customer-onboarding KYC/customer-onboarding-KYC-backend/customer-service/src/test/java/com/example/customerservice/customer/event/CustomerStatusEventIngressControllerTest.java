package com.example.customerservice.customer.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.example.customerservice.customer.model.OnboardingStatus;
import com.example.customerservice.customer.service.CustomerService;

@ExtendWith(MockitoExtension.class)
class CustomerStatusEventIngressControllerTest {

    @Mock
    private CustomerService customerService;

    private CustomerStatusEventIngressController controller;

    @BeforeEach
    void setUp() {
        controller = new CustomerStatusEventIngressController(customerService);
    }

    @Test
    void onManualApprovalRequired_updatesStatusAndReturnsAccepted() {
        UUID customerId = UUID.randomUUID();

        var response = controller.onManualApprovalRequired(
                new CustomerStatusEventIngressController.UpdateOnboardingStatusRequest(customerId, "MANUAL_APPROVAL_REQUIRED"));

        verify(customerService).updateOnboardingStatus(customerId, OnboardingStatus.MANUAL_APPROVAL_REQUIRED);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void onApplicationRejected_updatesStatusAndReturnsAccepted() {
        UUID customerId = UUID.randomUUID();

        var response = controller.onApplicationRejected(
                new CustomerStatusEventIngressController.UpdateOnboardingStatusRequest(customerId, "REJECTED"));

        verify(customerService).updateOnboardingStatus(customerId, OnboardingStatus.REJECTED);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }

    @Test
    void onAccountApproved_updatesStatusAndReturnsAccepted() {
        UUID customerId = UUID.randomUUID();

        var response = controller.onAccountApproved(
                new CustomerStatusEventIngressController.UpdateOnboardingStatusRequest(customerId, "APPROVED"));

        verify(customerService).updateOnboardingStatus(customerId, OnboardingStatus.APPROVED);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
    }
}
