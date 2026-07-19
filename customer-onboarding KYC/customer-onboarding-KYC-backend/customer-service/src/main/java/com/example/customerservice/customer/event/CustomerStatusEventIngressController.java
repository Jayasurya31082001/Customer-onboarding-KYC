package com.example.customerservice.customer.event;

import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.customerservice.customer.model.OnboardingStatus;
import com.example.customerservice.customer.service.CustomerService;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/internal/events")
@Validated
public class CustomerStatusEventIngressController {

    private final CustomerService customerService;

    public CustomerStatusEventIngressController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/manual-approval-required")
    public ResponseEntity<Void> onManualApprovalRequired(@Valid @RequestBody UpdateOnboardingStatusRequest request) {
        customerService.updateOnboardingStatus(request.customerId(), OnboardingStatus.MANUAL_APPROVAL_REQUIRED);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/application-rejected")
    public ResponseEntity<Void> onApplicationRejected(@Valid @RequestBody UpdateOnboardingStatusRequest request) {
        customerService.updateOnboardingStatus(request.customerId(), OnboardingStatus.REJECTED);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/account-approved")
    public ResponseEntity<Void> onAccountApproved(@Valid @RequestBody UpdateOnboardingStatusRequest request) {
        customerService.updateOnboardingStatus(request.customerId(), OnboardingStatus.APPROVED);
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    public record UpdateOnboardingStatusRequest(
            @NotNull(message = "customerId is required")
            UUID customerId,

            @NotNull(message = "status is required")
            String status
    ) {}
}
