package com.example.notificationservice.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.notificationservice.service.NotificationEventIngressService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/internal/events")
@Validated
public class NotificationEventIngressController {

    private static final Logger LOGGER = LoggerFactory.getLogger(NotificationEventIngressController.class);

    private final NotificationEventIngressService notificationEventIngressService;

    public NotificationEventIngressController(NotificationEventIngressService notificationEventIngressService) {
        this.notificationEventIngressService = notificationEventIngressService;
    }

    @PostMapping("/account-created")
    public ResponseEntity<Void> onAccountCreated(@Valid @RequestBody AccountCreatedIngressRequest request) {
        LOGGER.info("Received account-created notification event. customerId={}, accountNumber={}",
            request.customerId(), request.accountNumber());
        notificationEventIngressService.onAccountCreated(
                request.customerId(),
                request.customerEmail(),
                request.accountNumber(),
                request.sortCode());
        LOGGER.info("Accepted account-created notification event. customerId={}", request.customerId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }

    @PostMapping("/application-rejected")
    public ResponseEntity<Void> onApplicationRejected(@Valid @RequestBody ApplicationRejectedIngressRequest request) {
        LOGGER.info("Received application-rejected notification event. customerId={}", request.customerId());
        notificationEventIngressService.onApplicationRejected(
                request.customerId(),
                request.customerEmail(),
                request.reason());
        LOGGER.info("Accepted application-rejected notification event. customerId={}", request.customerId());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
