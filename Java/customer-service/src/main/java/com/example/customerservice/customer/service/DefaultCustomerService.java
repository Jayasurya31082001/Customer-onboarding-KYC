package com.example.customerservice.customer.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.customerservice.common.exception.DuplicateEmailException;
import com.example.customerservice.common.exception.ResourceNotFoundException;
import com.example.customerservice.config.CacheConfig;
import com.example.customerservice.customer.dto.CreateCustomerRequest;
import com.example.customerservice.customer.dto.CustomerCreatedResponse;
import com.example.customerservice.customer.dto.CustomerResponse;
import com.example.customerservice.customer.event.CustomerRegisteredEvent;
import com.example.customerservice.customer.model.Customer;
import com.example.customerservice.customer.model.OnboardingStatus;
import com.example.customerservice.customer.repository.CustomerRepository;

@Service
public class DefaultCustomerService implements CustomerService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DefaultCustomerService.class);

    private final CustomerRepository customerRepository;
    private final ApplicationEventPublisher applicationEventPublisher;

    public DefaultCustomerService(CustomerRepository customerRepository,
                                  ApplicationEventPublisher applicationEventPublisher) {
        this.customerRepository = customerRepository;
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    @Transactional
    public CustomerCreatedResponse registerCustomer(CreateCustomerRequest request) {
        String normalizedEmail = normalizeEmail(request.email());
        if (customerRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateEmailException("Email address already registered");
        }

        LocalDateTime now = LocalDateTime.now();
        Customer customer = new Customer();
        customer.setCustomerId(UUID.randomUUID());
        customer.setFirstName(request.firstName().trim());
        customer.setLastName(request.lastName().trim());
        customer.setEmail(normalizedEmail);
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setPhoneNumber(request.phoneNumber().trim());
        customer.setNationality(request.nationality().trim().toUpperCase(Locale.ROOT));
        customer.setAddressLine1(request.addressLine1().trim());
        customer.setCity(request.city().trim());
        customer.setPostcode(request.postcode().trim().toUpperCase(Locale.UK));
        customer.setStatus(OnboardingStatus.PENDING);
        customer.setCreatedAt(now);

        try {
            Customer savedCustomer = customerRepository.save(customer);
            applicationEventPublisher.publishEvent(new CustomerRegisteredEvent(
                    savedCustomer.getCustomerId(),
                    savedCustomer.getEmail(),
                    now
            ));
            return new CustomerCreatedResponse(
                    savedCustomer.getCustomerId(),
                    savedCustomer.getStatus(),
                    savedCustomer.getCreatedAt()
            );
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateEmailException("Email address already registered");
        }
    }

    @Override
    @Transactional(readOnly = true)
    @Cacheable(cacheNames = CacheConfig.CUSTOMERS_CACHE, key = "#customerId")
    public CustomerResponse getCustomer(UUID customerId) {
        Customer customer = customerRepository.findById(customerId)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Customer not found with id: " + customerId));
        return toCustomerResponse(customer);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CustomerResponse> getCustomersByStatus(OnboardingStatus status) {
        List<CustomerResponse> customers = customerRepository.findAllByStatus(status)
                .stream()
                .map(this::toCustomerResponse)
                .toList();
        LOGGER.info("Resolved customers by status. status={}, count={}", status, customers.size());
        return customers;
    }

    @Override
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.CUSTOMERS_CACHE, key = "#customerId")
    public CustomerResponse updateOnboardingStatus(UUID customerId, OnboardingStatus status) {
        Customer customer = customerRepository.findById(customerId)
            .orElseThrow(() -> new ResourceNotFoundException(
                "Customer not found with id: " + customerId));

        customer.setStatus(status);
        Customer savedCustomer = customerRepository.save(customer);
        return toCustomerResponse(savedCustomer);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private CustomerResponse toCustomerResponse(Customer customer) {
        return new CustomerResponse(
                customer.getCustomerId(),
                customer.getFirstName(),
                customer.getLastName(),
                customer.getEmail(),
                customer.getDateOfBirth(),
                customer.getPhoneNumber(),
                customer.getNationality(),
                customer.getAddressLine1(),
                customer.getCity(),
                customer.getPostcode(),
                customer.getStatus(),
                customer.getCreatedAt()
        );
    }
}

