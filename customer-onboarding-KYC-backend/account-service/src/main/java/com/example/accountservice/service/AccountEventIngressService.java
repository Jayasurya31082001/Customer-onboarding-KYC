package com.example.accountservice.service;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.accountservice.events.RiskAssessedEvent;
import com.example.accountservice.model.Disposition;

@Service
public class AccountEventIngressService {

    private final ApplicationEventPublisher applicationEventPublisher;

    public AccountEventIngressService(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Transactional
    public void onRiskAssessed(String customerId, String customerEmail, Disposition disposition, int score) {
        applicationEventPublisher.publishEvent(new RiskAssessedEvent(this, customerId, customerEmail, disposition, score));
    }
}
