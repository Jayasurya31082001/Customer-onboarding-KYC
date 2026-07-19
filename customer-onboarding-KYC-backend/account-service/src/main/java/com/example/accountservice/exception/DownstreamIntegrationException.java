package com.example.accountservice.exception;

public class DownstreamIntegrationException extends RuntimeException {

    public DownstreamIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
