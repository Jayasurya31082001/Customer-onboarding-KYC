package com.example.customerservice.customer.event;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerRegisteredEvent(
        UUID customerId,
        String email,
        LocalDateTime occurredAt
) {
}

