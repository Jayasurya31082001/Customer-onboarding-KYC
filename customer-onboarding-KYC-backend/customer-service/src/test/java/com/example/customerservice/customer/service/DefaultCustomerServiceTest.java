package com.example.customerservice.customer.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;

import com.example.customerservice.common.exception.DuplicateEmailException;
import com.example.customerservice.common.exception.ResourceNotFoundException;
import com.example.customerservice.customer.dto.CreateCustomerRequest;
import com.example.customerservice.customer.dto.CustomerCreatedResponse;
import com.example.customerservice.customer.dto.CustomerResponse;
import com.example.customerservice.customer.event.CustomerRegisteredEvent;
import com.example.customerservice.customer.model.Customer;
import com.example.customerservice.customer.model.OnboardingStatus;
import com.example.customerservice.customer.repository.CustomerRepository;

@ExtendWith(MockitoExtension.class)
class DefaultCustomerServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    @InjectMocks
    private DefaultCustomerService customerService;

    @Captor
    private ArgumentCaptor<Customer> customerCaptor;

    @Captor
    private ArgumentCaptor<CustomerRegisteredEvent> eventCaptor;

    @Test
    @DisplayName("registerCustomer valid request persists pending customer and publishes event")
    void registerCustomer_validRequest_persistsPendingCustomerAndPublishesEvent() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Alice",
                "Walker",
                " Alice.Walker@Example.com ",
                LocalDate.of(1990, 2, 14),
                "+447911123456",
                "gb",
                "221B Baker Street",
                "London",
                "sw1a 1aa"
        );

        when(customerRepository.existsByEmail("alice.walker@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerCreatedResponse response = customerService.registerCustomer(request);

        verify(customerRepository).save(customerCaptor.capture());
        verify(applicationEventPublisher).publishEvent(eventCaptor.capture());

        Customer savedCustomer = customerCaptor.getValue();
        CustomerRegisteredEvent event = eventCaptor.getValue();

        assertThat(savedCustomer.getCustomerId()).isNotNull();
        assertThat(savedCustomer.getFirstName()).isEqualTo("Alice");
        assertThat(savedCustomer.getLastName()).isEqualTo("Walker");
        assertThat(savedCustomer.getEmail()).isEqualTo("alice.walker@example.com");
        assertThat(savedCustomer.getNationality()).isEqualTo("GB");
        assertThat(savedCustomer.getPostcode()).isEqualTo("SW1A 1AA");
        assertThat(savedCustomer.getStatus()).isEqualTo(OnboardingStatus.PENDING);
        assertThat(savedCustomer.getCreatedAt()).isNotNull();

        assertThat(response.customerId()).isEqualTo(savedCustomer.getCustomerId());
        assertThat(response.status()).isEqualTo(OnboardingStatus.PENDING);
        assertThat(response.createdAt()).isEqualTo(savedCustomer.getCreatedAt());

        assertThat(event.customerId()).isEqualTo(savedCustomer.getCustomerId());
        assertThat(event.email()).isEqualTo("alice.walker@example.com");
        assertThat(event.occurredAt()).isNotNull();
    }

    @Test
    @DisplayName("registerCustomer duplicate email throws conflict exception")
    void registerCustomer_duplicateEmail_throwsConflictException() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Alice",
                "Walker",
                "alice.walker@example.com",
                LocalDate.of(1990, 2, 14),
                "+447911123456",
                "GB",
                "221B Baker Street",
                "London",
                "SW1A 1AA"
        );

        when(customerRepository.existsByEmail("alice.walker@example.com")).thenReturn(true);

        assertThatThrownBy(() -> customerService.registerCustomer(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email address already registered");

        verify(customerRepository, never()).save(any(Customer.class));
        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("registerCustomer unique constraint failure is converted to duplicate email exception")
    void registerCustomer_uniqueConstraintViolation_throwsConflictException() {
        CreateCustomerRequest request = new CreateCustomerRequest(
                "Alice",
                "Walker",
                "alice.walker@example.com",
                LocalDate.of(1990, 2, 14),
                "+447911123456",
                "GB",
                "221B Baker Street",
                "London",
                "SW1A 1AA"
        );

        when(customerRepository.existsByEmail("alice.walker@example.com")).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenThrow(new DataIntegrityViolationException("duplicate"));

        assertThatThrownBy(() -> customerService.registerCustomer(request))
                .isInstanceOf(DuplicateEmailException.class)
                .hasMessage("Email address already registered");

        verify(applicationEventPublisher, never()).publishEvent(any());
    }

    @Test
    @DisplayName("getCustomer returns mapped response for existing customer")
    void getCustomer_existingCustomer_returnsMappedResponse() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));

        CustomerResponse response = customerService.getCustomer(customerId);

        assertThat(response.customerId()).isEqualTo(customerId);
        assertThat(response.firstName()).isEqualTo("Alice");
        assertThat(response.lastName()).isEqualTo("Walker");
        assertThat(response.email()).isEqualTo("alice.walker@example.com");
        assertThat(response.dateOfBirth()).isEqualTo(LocalDate.of(1990, 2, 14));
        assertThat(response.phoneNumber()).isEqualTo("+447911123456");
        assertThat(response.nationality()).isEqualTo("GB");
        assertThat(response.addressLine1()).isEqualTo("221B Baker Street");
        assertThat(response.city()).isEqualTo("London");
        assertThat(response.postcode()).isEqualTo("SW1A 1AA");
        assertThat(response.status()).isEqualTo(OnboardingStatus.PENDING);
        assertThat(response.createdAt()).isNotNull();
    }

    @Test
    @DisplayName("getCustomer throws ResourceNotFoundException when customer does not exist")
    void getCustomer_unknownCustomer_throwsResourceNotFoundException() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.getCustomer(customerId))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(customerId.toString());
    }

    @Test
    @DisplayName("updateOnboardingStatus updates status for existing customer")
    void updateOnboardingStatus_existingCustomer_updatesStatus() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);
        when(customerRepository.findById(customerId)).thenReturn(Optional.of(customer));
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = customerService.updateOnboardingStatus(customerId, OnboardingStatus.KYC_COMPLETED);

        verify(customerRepository).save(customer);
        assertThat(customer.getStatus()).isEqualTo(OnboardingStatus.KYC_COMPLETED);
        assertThat(response.status()).isEqualTo(OnboardingStatus.KYC_COMPLETED);
    }

    @Test
    @DisplayName("updateOnboardingStatus throws ResourceNotFoundException for unknown customer")
    void updateOnboardingStatus_unknownCustomer_throwsResourceNotFoundException() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findById(customerId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> customerService.updateOnboardingStatus(customerId, OnboardingStatus.KYC_IN_PROGRESS))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining(customerId.toString());

        verify(customerRepository, never()).save(any(Customer.class));
    }

    @Test
    @DisplayName("getCustomersByStatus returns mapped responses for requested status")
    void getCustomersByStatus_returnsMappedResponses() {
        UUID customerId = UUID.randomUUID();
        Customer customer = buildCustomer(customerId);
        customer.setStatus(OnboardingStatus.MANUAL_APPROVAL_REQUIRED);

        when(customerRepository.findAllByStatus(OnboardingStatus.MANUAL_APPROVAL_REQUIRED))
                .thenReturn(List.of(customer));

        List<CustomerResponse> responses = customerService.getCustomersByStatus(OnboardingStatus.MANUAL_APPROVAL_REQUIRED);

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).customerId()).isEqualTo(customerId);
        assertThat(responses.get(0).status()).isEqualTo(OnboardingStatus.MANUAL_APPROVAL_REQUIRED);
    }

    // ── helpers ──────────────────────────────────────────────────────────────

    private Customer buildCustomer(UUID customerId) {
        Customer customer = new Customer();
        customer.setCustomerId(customerId);
        customer.setFirstName("Alice");
        customer.setLastName("Walker");
        customer.setEmail("alice.walker@example.com");
        customer.setDateOfBirth(LocalDate.of(1990, 2, 14));
        customer.setPhoneNumber("+447911123456");
        customer.setNationality("GB");
        customer.setAddressLine1("221B Baker Street");
        customer.setCity("London");
        customer.setPostcode("SW1A 1AA");
        customer.setStatus(OnboardingStatus.PENDING);
        customer.setCreatedAt(LocalDateTime.now());
        return customer;
    }
}

