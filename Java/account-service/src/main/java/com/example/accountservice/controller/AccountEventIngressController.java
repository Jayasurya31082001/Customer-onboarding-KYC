package com.example.accountservice.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.accountservice.service.AccountEventIngressService;

import jakarta.validation.Valid;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/internal/events")
@Validated
public class AccountEventIngressController {

    private final AccountEventIngressService accountEventIngressService;

    public AccountEventIngressController(AccountEventIngressService accountEventIngressService) {
        this.accountEventIngressService = accountEventIngressService;
    }

    @PostMapping("/risk-assessed")
    public ResponseEntity<Void> onRiskAssessed(@Valid @RequestBody RiskAssessedIngressRequest request) {
        accountEventIngressService.onRiskAssessed(
                request.customerId(),
                request.customerEmail(),
                request.disposition(),
                request.score());
        return ResponseEntity.status(HttpStatus.ACCEPTED).build();
    }
}
