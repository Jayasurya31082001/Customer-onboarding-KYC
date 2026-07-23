package com.example.accountservice.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.example.accountservice.model.Account;

public interface AccountRepository extends JpaRepository<Account, String> {

    Optional<Account> findByCustomerId(String customerId);

    boolean existsByAccountNumber(String accountNumber);

    /** Incremental sync query — returns accounts created after the given timestamp. */
    Page<Account> findByCreatedAtAfter(LocalDateTime createdAfter, Pageable pageable);
}
