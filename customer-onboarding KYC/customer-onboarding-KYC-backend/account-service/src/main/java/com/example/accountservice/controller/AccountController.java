package com.example.accountservice.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.accountservice.model.Account;
import com.example.accountservice.service.AccountQueryService;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/v1/accounts")
@Validated
public class AccountController {

    private final AccountQueryService accountQueryService;

    public AccountController(AccountQueryService accountQueryService) {
        this.accountQueryService = accountQueryService;
    }

    @GetMapping("/customer/{customerId}")
    public ResponseEntity<AccountResponse> getByCustomerId(@PathVariable String customerId) {
        Account account = accountQueryService.getByCustomerId(customerId);
        return ResponseEntity.ok(new AccountResponse(
                account.getAccountId(),
                account.getCustomerId(),
                account.getAccountNumber(),
                account.getSortCode(),
                account.getAccountType(),
                account.getStatus(),
                account.getCreatedAt()
        ));
    }
}
