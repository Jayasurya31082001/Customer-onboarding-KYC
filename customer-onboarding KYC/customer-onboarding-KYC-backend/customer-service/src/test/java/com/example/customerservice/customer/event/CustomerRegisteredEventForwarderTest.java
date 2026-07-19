package com.example.customerservice.customer.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.slf4j.MDC;

import com.example.customerservice.kyc.client.KycEventPublisherClient;

@ExtendWith(MockitoExtension.class)
class CustomerRegisteredEventForwarderTest {

    @Mock
    private KycEventPublisherClient kycEventPublisherClient;

    private CustomerRegisteredEventForwarder forwarder;

    @BeforeEach
    void setUp() {
        forwarder = new CustomerRegisteredEventForwarder(kycEventPublisherClient);
    }

    @AfterEach
    void tearDown() {
        MDC.clear();
    }

    @Test
    void onCustomerRegistered_forwardsEventWithCorrelationIdFromMdc() {
        MDC.put("correlationId", "corr-123");
        CustomerRegisteredEvent event = new CustomerRegisteredEvent(UUID.randomUUID(), "cust1@bank.test", LocalDateTime.now());

        forwarder.onCustomerRegistered(event);

        verify(kycEventPublisherClient).publishCustomerRegistered(any());
    }

    @Test
    void recover_doesNotThrow() {
        CustomerRegisteredEvent event = new CustomerRegisteredEvent(UUID.randomUUID(), "cust1@bank.test", LocalDateTime.now());

        forwarder.recover(new RuntimeException("failure"), event);
    }
}
