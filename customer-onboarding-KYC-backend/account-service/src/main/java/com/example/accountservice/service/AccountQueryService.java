package com.example.accountservice.service;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.accountservice.exception.ResourceNotFoundException;
import com.example.accountservice.model.Account;
import com.example.accountservice.repository.AccountRepository;

@Service
public class AccountQueryService {

    private final AccountRepository accountRepository;

    public AccountQueryService(AccountRepository accountRepository) {
        this.accountRepository = accountRepository;
    }

    @Transactional(readOnly = true)
    @Cacheable(value = "accounts", key = "#customerId")
    public Account getByCustomerId(String customerId) {
        return accountRepository.findByCustomerId(customerId)
                .orElseThrow(() -> new ResourceNotFoundException("Account not found for customerId=" + customerId));
    }
}
