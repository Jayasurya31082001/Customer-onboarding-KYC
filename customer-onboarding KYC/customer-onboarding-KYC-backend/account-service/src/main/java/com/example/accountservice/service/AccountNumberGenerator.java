package com.example.accountservice.service;

import java.security.SecureRandom;

import org.springframework.stereotype.Component;

@Component
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();

    public String generate() {
        int value = RANDOM.nextInt(100_000_000);
        return String.format("%08d", value);
    }
}
