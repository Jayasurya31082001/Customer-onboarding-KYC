package com.example.customerservice.kyc.client;

import java.time.LocalDateTime;
import java.util.UUID;

public record CustomerRegisteredIngressRequest(
        UUID customerId,
        LocalDateTime occurredAt,
        String correlationId
) {
}
