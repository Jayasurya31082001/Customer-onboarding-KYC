package com.example.accountservice.service;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.api.RepeatedTest;

class AccountNumberGeneratorTest {

    private final AccountNumberGenerator generator = new AccountNumberGenerator();

    @RepeatedTest(20)
    void generate_returnsEightDigitNumericValue() {
        String generated = generator.generate();

        assertThat(generated).hasSize(8);
        assertThat(generated).matches("\\d{8}");
    }
}
