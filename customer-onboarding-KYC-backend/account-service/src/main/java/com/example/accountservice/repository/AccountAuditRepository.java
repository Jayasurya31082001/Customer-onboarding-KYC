package com.example.accountservice.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.accountservice.model.AccountAudit;

public interface AccountAuditRepository extends JpaRepository<AccountAudit, Long> {
}
