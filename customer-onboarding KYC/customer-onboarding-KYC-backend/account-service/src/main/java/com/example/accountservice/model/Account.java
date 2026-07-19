package com.example.accountservice.model;

import java.time.LocalDateTime;

import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "accounts")
@EntityListeners(AuditingEntityListener.class)
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Account {

    @Id
    @UuidGenerator
    @Column(name = "account_id", nullable = false, length = 36, updatable = false)
    private String accountId;

    @Column(name = "customer_id", nullable = false, length = 36, unique = true)
    private String customerId;

    @Column(name = "account_number", nullable = false, length = 8, unique = true)
    private String accountNumber;

    @Column(name = "sort_code", nullable = false, length = 8)
    private String sortCode;

    @Column(name = "account_type", nullable = false, length = 20)
    private String accountType;

    @Column(name = "status", nullable = false, length = 20)
    private String status;

    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
}
