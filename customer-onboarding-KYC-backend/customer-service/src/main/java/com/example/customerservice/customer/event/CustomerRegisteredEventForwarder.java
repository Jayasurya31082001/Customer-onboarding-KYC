package com.example.customerservice.customer.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.customerservice.kyc.client.CustomerRegisteredIngressRequest;
import com.example.customerservice.kyc.client.KycEventPublisherClient;

@Component
public class CustomerRegisteredEventForwarder {

    private static final Logger LOGGER = LoggerFactory.getLogger(CustomerRegisteredEventForwarder.class);

    private final KycEventPublisherClient kycEventPublisherClient;

    public CustomerRegisteredEventForwarder(KycEventPublisherClient kycEventPublisherClient) {
        this.kycEventPublisherClient = kycEventPublisherClient;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Retryable(maxAttempts = 3, backoff = @Backoff(delay = 500, multiplier = 2.0))
    public void onCustomerRegistered(CustomerRegisteredEvent event) {
        String correlationId = MDC.get("correlationId");
        kycEventPublisherClient.publishCustomerRegistered(new CustomerRegisteredIngressRequest(
                event.customerId(),
                event.occurredAt(),
                correlationId
        ));

        LOGGER.info("Forwarded customer-registered event to kyc-service. customerId={}, correlationId={}",
                event.customerId(), correlationId);
    }

    @Recover
    public void recover(Exception exception, CustomerRegisteredEvent event) {
        LOGGER.error("Failed to forward customer-registered event after retries. customerId={}",
                event.customerId(), exception);
    }
}
