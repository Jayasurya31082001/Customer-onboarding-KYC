package com.example.accountservice.events;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.accountservice.model.Account;
import com.example.accountservice.model.Disposition;
import com.example.accountservice.service.AccountProvisioningService;

@Component
public class AccountEventListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountEventListener.class);

    private final AccountProvisioningService accountCreationService;
    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountEventListener(AccountProvisioningService accountCreationService,
                                                                ApplicationEventPublisher applicationEventPublisher) {
        this.accountCreationService = accountCreationService;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Async("accountTaskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleRiskAssessed(RiskAssessedEvent event) {
        LOGGER.info("Received risk assessed event. customerId={}, disposition={}, score={}",
                event.getCustomerId(), event.getDisposition(), event.getScore());

        if (event.getDisposition() != Disposition.AUTO_APPROVE) {
            LOGGER.info("Skipping account creation for non-approved disposition. customerId={}, disposition={}",
                    event.getCustomerId(), event.getDisposition());
            return;
        }

        Account account = accountCreationService.createAccountForCustomer(event.getCustomerId(), null);

        applicationEventPublisher.publishEvent(new AccountCreatedEvent(
                this,
                account.getCustomerId(),
                event.getCustomerEmail(),
                account.getAccountNumber(),
                account.getSortCode()
        ));

        LOGGER.info("Published account created event. customerId={}, accountNumber={}",
                account.getCustomerId(), account.getAccountNumber());
    }
}
