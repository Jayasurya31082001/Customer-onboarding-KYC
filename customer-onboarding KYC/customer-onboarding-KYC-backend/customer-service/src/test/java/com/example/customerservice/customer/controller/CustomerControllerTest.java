package com.example.customerservice.customer.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.customerservice.common.exception.DuplicateEmailException;
import com.example.customerservice.common.exception.ResourceNotFoundException;
import com.example.customerservice.customer.dto.CustomerCreatedResponse;
import com.example.customerservice.customer.dto.CustomerResponse;
import com.example.customerservice.customer.model.OnboardingStatus;
import com.example.customerservice.customer.service.CustomerService;

@WebMvcTest(CustomerController.class)
class CustomerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private CustomerService customerService;

    // ── POST /api/v1/customers ──────────────────────────────────────────────

    @Test
    @DisplayName("POST /api/v1/customers returns 201 with Location header for valid request")
    void registerCustomer_validRequest_returns201WithLocation() throws Exception {
        UUID customerId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        when(customerService.registerCustomer(any()))
                .thenReturn(new CustomerCreatedResponse(customerId, OnboardingStatus.PENDING, now));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterRequestJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", org.hamcrest.Matchers.containsString(customerId.toString())))
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("POST /api/v1/customers returns 400 when firstName is blank")
    void registerCustomer_blankFirstName_returns400() throws Exception {
        String body = """
                {
                    "firstName": "",
                    "lastName": "Walker",
                    "email": "alice.walker@example.com",
                    "dateOfBirth": "1990-02-14",
                    "phoneNumber": "+447911123456",
                    "nationality": "GB",
                    "addressLine1": "221B Baker Street",
                    "city": "London",
                    "postcode": "SW1A 1AA"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    @DisplayName("POST /api/v1/customers returns 400 when email is invalid")
    void registerCustomer_invalidEmail_returns400() throws Exception {
        String body = """
                {
                    "firstName": "Alice",
                    "lastName": "Walker",
                    "email": "not-an-email",
                    "dateOfBirth": "1990-02-14",
                    "phoneNumber": "+447911123456",
                    "nationality": "GB",
                    "addressLine1": "221B Baker Street",
                    "city": "London",
                    "postcode": "SW1A 1AA"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.email").exists());
    }

    @Test
    @DisplayName("POST /api/v1/customers returns 400 when customer is a minor")
    void registerCustomer_minorDateOfBirth_returns400() throws Exception {
        String underageDate = LocalDate.now().minusYears(17).toString();
        String body = """
                {
                    "firstName": "Alice",
                    "lastName": "Walker",
                    "email": "alice.walker@example.com",
                    "dateOfBirth": "%s",
                    "phoneNumber": "+447911123456",
                    "nationality": "GB",
                    "addressLine1": "221B Baker Street",
                    "city": "London",
                    "postcode": "SW1A 1AA"
                }
                """.formatted(underageDate);

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.dateOfBirth").exists());
    }

    @Test
    @DisplayName("POST /api/v1/customers returns 400 when phone number format is invalid")
    void registerCustomer_invalidPhoneFormat_returns400() throws Exception {
        String body = """
                {
                    "firstName": "Alice",
                    "lastName": "Walker",
                    "email": "alice.walker@example.com",
                    "dateOfBirth": "1990-02-14",
                    "phoneNumber": "07911123456",
                    "nationality": "GB",
                    "addressLine1": "221B Baker Street",
                    "city": "London",
                    "postcode": "SW1A 1AA"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.phoneNumber").exists());
    }

    @Test
    @DisplayName("POST /api/v1/customers returns 400 when nationality is not a valid ISO code")
    void registerCustomer_invalidNationality_returns400() throws Exception {
        String body = """
                {
                    "firstName": "Alice",
                    "lastName": "Walker",
                    "email": "alice.walker@example.com",
                    "dateOfBirth": "1990-02-14",
                    "phoneNumber": "+447911123456",
                    "nationality": "XX",
                    "addressLine1": "221B Baker Street",
                    "city": "London",
                    "postcode": "SW1A 1AA"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.nationality").exists());
    }

    @Test
    @DisplayName("POST /api/v1/customers returns 400 when postcode is invalid")
    void registerCustomer_invalidPostcode_returns400() throws Exception {
        String body = """
                {
                    "firstName": "Alice",
                    "lastName": "Walker",
                    "email": "alice.walker@example.com",
                    "dateOfBirth": "1990-02-14",
                    "phoneNumber": "+447911123456",
                    "nationality": "GB",
                    "addressLine1": "221B Baker Street",
                    "city": "London",
                    "postcode": "12345"
                }
                """;

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.postcode").exists());
    }

    @Test
    @DisplayName("POST /api/v1/customers returns 409 when email is already registered")
    void registerCustomer_duplicateEmail_returns409() throws Exception {
        when(customerService.registerCustomer(any()))
                .thenThrow(new DuplicateEmailException("Email address already registered"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterRequestJson()))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.title").value("Duplicate email"))
                .andExpect(jsonPath("$.detail").value("Email address already registered"));
    }

    @Test
    @DisplayName("POST /api/v1/customers returns 500 on unexpected error")
    void registerCustomer_unexpectedError_returns500() throws Exception {
        when(customerService.registerCustomer(any()))
                .thenThrow(new RuntimeException("Unexpected failure"));

        mockMvc.perform(post("/api/v1/customers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validRegisterRequestJson()))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.title").value("Internal server error"));
    }

    // ── GET /api/v1/customers/{customerId} ─────────────────────────────────

    @Test
    @DisplayName("GET /api/v1/customers?status=MANUAL_APPROVAL_REQUIRED returns filtered customers")
    void getCustomersByStatus_returnsFilteredCustomers() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                customerId, "Alice", "Walker", "alice.walker@example.com",
                LocalDate.of(1990, 2, 14), "+447911123456", "GB",
                "221B Baker Street", "London", "SW1A 1AA",
                OnboardingStatus.MANUAL_APPROVAL_REQUIRED, LocalDateTime.now()
        );
        when(customerService.getCustomersByStatus(OnboardingStatus.MANUAL_APPROVAL_REQUIRED))
                .thenReturn(List.of(response));

        mockMvc.perform(get("/api/v1/customers").param("status", "MANUAL_APPROVAL_REQUIRED"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].customerId").value(customerId.toString()))
                .andExpect(jsonPath("$[0].status").value("MANUAL_APPROVAL_REQUIRED"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} returns 200 with full customer profile")
    void getCustomer_existingCustomer_returns200() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                customerId, "Alice", "Walker", "alice.walker@example.com",
                LocalDate.of(1990, 2, 14), "+447911123456", "GB",
                "221B Baker Street", "London", "SW1A 1AA",
                OnboardingStatus.PENDING, LocalDateTime.now()
        );
        when(customerService.getCustomer(customerId)).thenReturn(response);

        mockMvc.perform(get("/api/v1/customers/{id}", customerId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.firstName").value("Alice"))
                .andExpect(jsonPath("$.lastName").value("Walker"))
                .andExpect(jsonPath("$.email").value("alice.walker@example.com"))
                .andExpect(jsonPath("$.nationality").value("GB"))
                .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} returns 404 when customer does not exist")
    void getCustomer_unknownCustomer_returns404() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.getCustomer(customerId))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: " + customerId));

        mockMvc.perform(get("/api/v1/customers/{id}", customerId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    @Test
    @DisplayName("GET /api/v1/customers/{id} returns 400 when customerId is not a valid UUID")
    void getCustomer_invalidUuidFormat_returns400() throws Exception {
        mockMvc.perform(get("/api/v1/customers/not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Invalid request parameter"));
    }

    // ── PATCH /api/v1/customers/{customerId}/status ─────────────────────────

    @Test
    @DisplayName("PATCH /api/v1/customers/{id}/status returns 200 with updated status")
    void updateOnboardingStatus_validRequest_returns200() throws Exception {
        UUID customerId = UUID.randomUUID();
        CustomerResponse response = new CustomerResponse(
                customerId, "Alice", "Walker", "alice.walker@example.com",
                LocalDate.of(1990, 2, 14), "+447911123456", "GB",
                "221B Baker Street", "London", "SW1A 1AA",
                OnboardingStatus.KYC_COMPLETED, LocalDateTime.now()
        );
        when(customerService.updateOnboardingStatus(customerId, OnboardingStatus.KYC_COMPLETED)).thenReturn(response);

        mockMvc.perform(patch("/api/v1/customers/{id}/status", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"KYC_COMPLETED\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.customerId").value(customerId.toString()))
                .andExpect(jsonPath("$.status").value("KYC_COMPLETED"));
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{id}/status returns 400 when status missing")
    void updateOnboardingStatus_missingStatus_returns400() throws Exception {
        UUID customerId = UUID.randomUUID();

        mockMvc.perform(patch("/api/v1/customers/{id}/status", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.title").value("Validation failed"));
    }

    @Test
    @DisplayName("PATCH /api/v1/customers/{id}/status returns 404 when customer does not exist")
    void updateOnboardingStatus_unknownCustomer_returns404() throws Exception {
        UUID customerId = UUID.randomUUID();
        when(customerService.updateOnboardingStatus(customerId, OnboardingStatus.KYC_IN_PROGRESS))
                .thenThrow(new ResourceNotFoundException("Customer not found with id: " + customerId));

        mockMvc.perform(patch("/api/v1/customers/{id}/status", customerId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"status\":\"KYC_IN_PROGRESS\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("Resource not found"));
    }

    // ── helpers ─────────────────────────────────────────────────────────────

    private String validRegisterRequestJson() {
        return """
                {
                    "firstName": "Alice",
                    "lastName": "Walker",
                    "email": "alice.walker@example.com",
                    "dateOfBirth": "1990-02-14",
                    "phoneNumber": "+447911123456",
                    "nationality": "GB",
                    "addressLine1": "221B Baker Street",
                    "city": "London",
                    "postcode": "SW1A 1AA"
                }
                """;
    }
}
