package com.example.accountservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.accountservice.corebanking.client.CoreBankingClient;
import com.example.accountservice.corebanking.client.CoreBankingProvisionResponse;
import com.example.accountservice.model.Account;
import com.example.accountservice.model.AccountAudit;
import com.example.accountservice.repository.AccountAuditRepository;
import com.example.accountservice.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountCreationServiceTest {

    @Mock
    private AccountRepository accountRepository;

    @Mock
    private AccountAuditRepository accountAuditRepository;

    @Mock
    private AccountNumberGenerator accountNumberGenerator;

    @Mock
    private CoreBankingClient coreBankingClient;

    private AccountCreationService service;

    @BeforeEach
    void setUp() {
        service = new AccountCreationService(
                accountRepository,
                accountAuditRepository,
                accountNumberGenerator,
                coreBankingClient);
    }

    @Test
    void createAccountForCustomer_createsAccountAndAudit() {
        when(accountNumberGenerator.generate()).thenReturn("11111111");
        when(accountRepository.existsByAccountNumber("11111111")).thenReturn(false);
        when(coreBankingClient.provisionAccount("cust-1", "corr-1")).thenReturn(new CoreBankingProvisionResponse("110011"));

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> {
            Account saved = invocation.getArgument(0);
            saved.setAccountId("acc-1");
            return saved;
        });

        Account created = service.createAccountForCustomer("cust-1", "corr-1");

        assertThat(created.getAccountId()).isEqualTo("acc-1");
        assertThat(created.getCustomerId()).isEqualTo("cust-1");
        assertThat(created.getAccountNumber()).isEqualTo("11111111");
        assertThat(created.getSortCode()).isEqualTo("110011");
        assertThat(created.getStatus()).isEqualTo("ACTIVE");

        ArgumentCaptor<AccountAudit> auditCaptor = ArgumentCaptor.forClass(AccountAudit.class);
        verify(accountAuditRepository).save(auditCaptor.capture());
        assertThat(auditCaptor.getValue().getAccountId()).isEqualTo("acc-1");
        assertThat(auditCaptor.getValue().getEventType()).isEqualTo("ACCOUNT_CREATED");
        assertThat(auditCaptor.getValue().getDetails()).contains("cust-1");
    }

    @Test
    void createAccountForCustomer_regeneratesWhenAccountNumberAlreadyExists() {
        when(accountNumberGenerator.generate()).thenReturn("11111111", "22222222");
        when(accountRepository.existsByAccountNumber("11111111")).thenReturn(true);
        when(accountRepository.existsByAccountNumber("22222222")).thenReturn(false);
        when(coreBankingClient.provisionAccount("cust-2", "corr-2")).thenReturn(new CoreBankingProvisionResponse("220022"));

        when(accountRepository.save(any(Account.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Account created = service.createAccountForCustomer("cust-2", "corr-2");

        assertThat(created.getAccountNumber()).isEqualTo("22222222");
        verify(accountNumberGenerator, times(2)).generate();
    }
}
