package com.example.accountservice.corebanking.client;

public class CoreBankingUnavailableException extends RuntimeException {

    public CoreBankingUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
