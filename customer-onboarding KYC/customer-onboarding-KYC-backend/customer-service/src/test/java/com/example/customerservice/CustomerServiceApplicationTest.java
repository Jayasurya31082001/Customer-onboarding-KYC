package com.example.customerservice;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class CustomerServiceApplicationTest {

    @Test
    void canInstantiateApplicationClass() {
        CustomerServiceApplication application = new CustomerServiceApplication();

        assertThat(application).isNotNull();
    }
}
