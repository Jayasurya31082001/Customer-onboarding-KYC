package com.example.customerservice.customer.service;

import java.util.List;
import java.util.UUID;

import com.example.customerservice.customer.dto.CreateCustomerRequest;
import com.example.customerservice.customer.dto.CustomerCreatedResponse;
import com.example.customerservice.customer.dto.CustomerResponse;
import com.example.customerservice.customer.model.OnboardingStatus;

public interface CustomerService {

    CustomerCreatedResponse registerCustomer(CreateCustomerRequest request);

    CustomerResponse getCustomer(UUID customerId);

    List<CustomerResponse> getCustomersByStatus(OnboardingStatus status);

    CustomerResponse updateOnboardingStatus(UUID customerId, OnboardingStatus status);
}

