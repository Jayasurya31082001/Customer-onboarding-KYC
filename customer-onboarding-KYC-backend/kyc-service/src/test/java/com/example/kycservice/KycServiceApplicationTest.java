package com.example.kycservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KycServiceApplicationTest {

    @Test
    void canInstantiateApplicationClass() {
        KycServiceApplication app = new KycServiceApplication();

        assertThat(app).isNotNull();
    }
}
