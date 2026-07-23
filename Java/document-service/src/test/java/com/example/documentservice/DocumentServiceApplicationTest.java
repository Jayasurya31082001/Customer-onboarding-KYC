package com.example.documentservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DocumentServiceApplicationTest {

    @Test
    void canInstantiateApplicationClass() {
        DocumentServiceApplication app = new DocumentServiceApplication();

        assertThat(app).isNotNull();
    }
}
