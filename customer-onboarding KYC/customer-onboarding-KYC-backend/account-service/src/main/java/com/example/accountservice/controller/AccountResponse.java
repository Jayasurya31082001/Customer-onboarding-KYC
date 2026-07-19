package com.example.accountservice.controller;

import java.time.LocalDateTime;

public record AccountResponse(
        String accountId,
        String customerId,
        String accountNumber,
        String sortCode,
        String accountType,
        String status,
        LocalDateTime createdAt
) {
}
