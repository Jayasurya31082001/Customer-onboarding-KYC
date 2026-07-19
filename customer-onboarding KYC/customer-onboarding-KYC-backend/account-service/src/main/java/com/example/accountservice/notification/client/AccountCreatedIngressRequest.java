package com.example.accountservice.notification.client;

public record AccountCreatedIngressRequest(String customerId, String customerEmail, String accountNumber, String sortCode) {

	public AccountCreatedIngressRequest {
		if (customerId == null || customerId.isBlank()) {
			throw new IllegalArgumentException("customerId is required");
		}
		if (customerEmail == null || customerEmail.isBlank()) {
			throw new IllegalArgumentException("customerEmail is required");
		}
		if (accountNumber == null || accountNumber.isBlank()) {
			throw new IllegalArgumentException("accountNumber is required");
		}
		if (sortCode == null || sortCode.isBlank()) {
			throw new IllegalArgumentException("sortCode is required");
		}
	}
}
