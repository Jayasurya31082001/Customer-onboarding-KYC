package com.example.accountservice.controller;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import com.example.accountservice.model.Account;
import com.example.accountservice.service.AccountQueryService;

@ExtendWith(MockitoExtension.class)
class AccountControllerTest {

    @Mock
    private AccountQueryService accountQueryService;

    private AccountController controller;

    @BeforeEach
    void setUp() {
        controller = new AccountController(accountQueryService);
    }

    @Test
    void getByCustomerId_returnsMappedResponse() {
        LocalDateTime createdAt = LocalDateTime.now();
        Account account = Account.builder()
                .accountId("acc-1")
                .customerId("cust-1")
                .accountNumber("12345678")
                .sortCode("110011")
                .accountType("CURRENT")
                .status("ACTIVE")
                .createdAt(createdAt)
                .build();

        when(accountQueryService.getByCustomerId("cust-1")).thenReturn(account);

        var response = controller.getByCustomerId("cust-1");

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isEqualTo(new AccountResponse(
                "acc-1",
                "cust-1",
                "12345678",
                "110011",
                "CURRENT",
                "ACTIVE",
                createdAt));
    }
}
