package com.example.accountservice.service;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.accountservice.exception.ResourceNotFoundException;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;

@ExtendWith(MockitoExtension.class)
class AccountQueryServiceTest {

    @Mock
    private AccountRepository accountRepository;

    private AccountQueryService service;

    @BeforeEach
    void setUp() {
        service = new AccountQueryService(accountRepository);
    }

    @Test
    void getByCustomerId_returnsAccountWhenFound() {
        Account account = Account.builder()
                .accountId("acc-1")
                .customerId("cust-1")
                .accountNumber("12345678")
                .sortCode("110011")
                .accountType("CURRENT")
                .status("ACTIVE")
                .createdAt(LocalDateTime.now())
                .build();
        when(accountRepository.findByCustomerId("cust-1")).thenReturn(Optional.of(account));

        Account result = service.getByCustomerId("cust-1");

        assertThat(result).isSameAs(account);
    }

    @Test
    void getByCustomerId_throwsWhenNotFound() {
        when(accountRepository.findByCustomerId("missing")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.getByCustomerId("missing"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("missing");
    }
}
