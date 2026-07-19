package com.example.accountservice.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.accountservice.corebanking.client.CoreBankingClient;
import com.example.accountservice.model.Account;
import com.example.accountservice.model.AccountAudit;
import com.example.accountservice.repository.AccountAuditRepository;
import com.example.accountservice.repository.AccountRepository;

@Service
public class AccountCreationService implements AccountProvisioningService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccountCreationService.class);

    private final AccountRepository accountRepository;
    private final AccountAuditRepository accountAuditRepository;
    private final AccountNumberGenerator accountNumberGenerator;
    private final CoreBankingClient coreBankingClient;

    public AccountCreationService(AccountRepository accountRepository,
                                  AccountAuditRepository accountAuditRepository,
                                  AccountNumberGenerator accountNumberGenerator,
                                  CoreBankingClient coreBankingClient) {
        this.accountRepository = accountRepository;
        this.accountAuditRepository = accountAuditRepository;
        this.accountNumberGenerator = accountNumberGenerator;
        this.coreBankingClient = coreBankingClient;
    }

    @Transactional
    @Override
    public Account createAccountForCustomer(String customerId, String correlationId) {
        String accountNumber = nextAccountNumber();
        String sortCode = coreBankingClient.provisionAccount(customerId, correlationId).sortCode();

        Account account = accountRepository.save(Account.builder()
                .customerId(customerId)
                .accountNumber(accountNumber)
                .sortCode(sortCode)
                .accountType("CURRENT")
                .status("ACTIVE")
                .build());

        accountAuditRepository.save(AccountAudit.builder()
                .accountId(account.getAccountId())
                .eventType("ACCOUNT_CREATED")
                .details("Account created for customer " + customerId)
                .build());

        LOGGER.info("Account created. accountId={}, customerId={}, accountNumber={}, correlationId={}",
                account.getAccountId(), customerId, accountNumber, correlationId);

        return account;
    }

    private String nextAccountNumber() {
        String generated;
        do {
            generated = accountNumberGenerator.generate();
        } while (accountRepository.existsByAccountNumber(generated));
        return generated;
    }
}
