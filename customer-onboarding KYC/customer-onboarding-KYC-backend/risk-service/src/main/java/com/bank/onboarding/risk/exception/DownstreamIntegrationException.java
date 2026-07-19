package com.bank.onboarding.risk.exception;

public class DownstreamIntegrationException extends RuntimeException {

    public DownstreamIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
