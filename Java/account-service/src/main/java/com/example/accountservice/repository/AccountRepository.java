package com.example.accountservice.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.accountservice.model.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByCustomerId(String customerId);

    boolean existsByAccountNumber(String accountNumber);
}
