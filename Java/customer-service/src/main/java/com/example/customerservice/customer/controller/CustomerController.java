package com.example.customerservice.customer.controller;

import java.net.URI;
import java.util.List;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import com.example.customerservice.common.api.ApiErrorResponse;
import com.example.customerservice.common.api.ValidationErrorResponse;
import com.example.customerservice.customer.dto.CreateCustomerRequest;
import com.example.customerservice.customer.dto.CustomerCreatedResponse;
import com.example.customerservice.customer.dto.CustomerEmailResponse;
import com.example.customerservice.customer.dto.CustomerResponse;
import com.example.customerservice.customer.dto.UpdateCustomerStatusRequest;
import com.example.customerservice.customer.model.OnboardingStatus;
import com.example.customerservice.customer.service.CustomerService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping({"/api/v1/customers"})
@Tag(name = "Customers", description = "Customer onboarding registration endpoints")
public class CustomerController {

        private static final Logger LOGGER = LoggerFactory.getLogger(CustomerController.class);

    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping
    @Operation(
            summary = "Register a new customer",
            description = "Registers a customer for onboarding and returns the generated customer identifier."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Customer registration payload",
            content = @Content(schema = @Schema(implementation = CreateCustomerRequest.class))
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "201",
                    description = "Customer registered successfully",
                    content = @Content(schema = @Schema(implementation = CustomerCreatedResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation failure",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "409",
                    description = "Email address already registered",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<CustomerCreatedResponse> registerCustomer(@Valid @RequestBody CreateCustomerRequest request) {
        CustomerCreatedResponse response = customerService.registerCustomer(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{customerId}")
                .buildAndExpand(response.customerId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @GetMapping("/{customerId}")
    @Operation(
            summary = "Retrieve a customer by ID",
            description = "Returns the full customer profile for the given customer identifier."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer found",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid customer ID format",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<CustomerResponse> getCustomer(
            @Parameter(description = "Unique customer identifier (UUID)", required = true, example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd")
            @PathVariable UUID customerId) {

        CustomerResponse response = customerService.getCustomer(customerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @Operation(
            summary = "Retrieve customers by onboarding status",
            description = "Returns customers filtered by the provided onboarding status."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Customers retrieved successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid status value",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<List<CustomerResponse>> getCustomersByStatus(
            @Parameter(description = "Filter by onboarding status", required = true, example = "MANUAL_APPROVAL_REQUIRED")
            @RequestParam OnboardingStatus status) {

        List<CustomerResponse> customers = customerService.getCustomersByStatus(status);
                LOGGER.info("Fetched customers by status. status={}, count={}", status, customers.size());
        return ResponseEntity.ok(customers);
    }

    @PatchMapping("/{customerId}/status")
    @Operation(
            summary = "Update customer onboarding status",
            description = "Updates onboarding status for an existing customer."
    )
    @io.swagger.v3.oas.annotations.parameters.RequestBody(
            required = true,
            description = "Status update payload",
            content = @Content(schema = @Schema(implementation = UpdateCustomerStatusRequest.class))
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer status updated successfully",
                    content = @Content(schema = @Schema(implementation = CustomerResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Invalid request payload",
                    content = @Content(schema = @Schema(implementation = ValidationErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<CustomerResponse> updateOnboardingStatus(
            @Parameter(description = "Unique customer identifier (UUID)", required = true, example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd")
            @PathVariable UUID customerId,
            @Valid @RequestBody UpdateCustomerStatusRequest request) {

        CustomerResponse response = customerService.updateOnboardingStatus(customerId, request.status());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{customerId}/email")
    @Operation(
            summary = "Retrieve customer email address",
            description = "Returns the email address for the given customer identifier (internal use only)."
    )
    @ApiResponses({
            @ApiResponse(
                    responseCode = "200",
                    description = "Customer email retrieved",
                    content = @Content(schema = @Schema(implementation = CustomerEmailResponse.class))
            ),
            @ApiResponse(
                    responseCode = "404",
                    description = "Customer not found",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "500",
                    description = "Unexpected internal server error",
                    content = @Content(schema = @Schema(implementation = ApiErrorResponse.class))
            )
    })
    public ResponseEntity<CustomerEmailResponse> getCustomerEmail(
            @Parameter(description = "Unique customer identifier (UUID)", required = true, example = "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd")
            @PathVariable UUID customerId) {

        CustomerResponse customer = customerService.getCustomer(customerId);
        return ResponseEntity.ok(new CustomerEmailResponse(customer.email()));
    }
}

