package com.example.kycservice.kyc.event;

import java.util.UUID;

public record ApplicationRejectedEvent(
		UUID customerId,
		String customerEmail,
		String reason,
		String correlationId
) {
}