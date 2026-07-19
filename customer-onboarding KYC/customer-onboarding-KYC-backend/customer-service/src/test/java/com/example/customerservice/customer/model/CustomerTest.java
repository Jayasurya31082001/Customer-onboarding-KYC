package com.example.customerservice.customer.model;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import org.junit.jupiter.api.Test;

class CustomerTest {

    @Test
    void prePersist_initializesDefaults() {
        Customer customer = new Customer();
        customer.setFirstName("Alice");
        customer.setLastName("Walker");
        customer.setEmail("alice@bank.test");
        customer.setDateOfBirth(LocalDate.of(1990, 2, 14));
        customer.setPhoneNumber("+447911123456");
        customer.setNationality("GB");
        customer.setAddressLine1("221B Baker Street");
        customer.setCity("London");
        customer.setPostcode("SW1A 1AA");

        customer.prePersist();

        assertThat(customer.getCustomerId()).isNotNull();
        assertThat(customer.getCreatedAt()).isNotNull();
        assertThat(customer.getStatus()).isEqualTo(OnboardingStatus.PENDING);
    }

    @Test
    void gettersAndSetters_workForAllFields() {
        Customer customer = new Customer();
        UUID id = UUID.randomUUID();
        LocalDateTime createdAt = LocalDateTime.now();

        customer.setCustomerId(id);
        customer.setFirstName("Alice");
        customer.setLastName("Walker");
        customer.setEmail("alice@bank.test");
        customer.setDateOfBirth(LocalDate.of(1990, 2, 14));
        customer.setPhoneNumber("+447911123456");
        customer.setNationality("GB");
        customer.setAddressLine1("221B Baker Street");
        customer.setCity("London");
        customer.setPostcode("SW1A 1AA");
        customer.setStatus(OnboardingStatus.KYC_IN_PROGRESS);
        customer.setCreatedAt(createdAt);

        assertThat(customer.getCustomerId()).isEqualTo(id);
        assertThat(customer.getFirstName()).isEqualTo("Alice");
        assertThat(customer.getLastName()).isEqualTo("Walker");
        assertThat(customer.getEmail()).isEqualTo("alice@bank.test");
        assertThat(customer.getDateOfBirth()).isEqualTo(LocalDate.of(1990, 2, 14));
        assertThat(customer.getPhoneNumber()).isEqualTo("+447911123456");
        assertThat(customer.getNationality()).isEqualTo("GB");
        assertThat(customer.getAddressLine1()).isEqualTo("221B Baker Street");
        assertThat(customer.getCity()).isEqualTo("London");
        assertThat(customer.getPostcode()).isEqualTo("SW1A 1AA");
        assertThat(customer.getStatus()).isEqualTo(OnboardingStatus.KYC_IN_PROGRESS);
        assertThat(customer.getCreatedAt()).isEqualTo(createdAt);
    }
}
