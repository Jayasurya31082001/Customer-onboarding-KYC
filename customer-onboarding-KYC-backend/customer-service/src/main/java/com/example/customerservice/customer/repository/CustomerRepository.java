package com.example.customerservice.customer.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.customerservice.customer.model.Customer;
import com.example.customerservice.customer.model.OnboardingStatus;

public interface CustomerRepository extends JpaRepository<Customer, UUID> {

    boolean existsByEmail(String email);

    List<Customer> findAllByStatus(OnboardingStatus status);
}

