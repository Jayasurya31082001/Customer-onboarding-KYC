package com.example.accountservice.events;

import org.springframework.context.ApplicationEvent;

import com.example.accountservice.model.Disposition;

public class RiskAssessedEvent extends ApplicationEvent {

    private final String customerId;
    private final String customerEmail;
    private final Disposition disposition;
    private final int score;

    public RiskAssessedEvent(Object source, String customerId, String customerEmail, Disposition disposition, int score) {
        super(source);
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.disposition = disposition;
        this.score = score;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public Disposition getDisposition() {
        return disposition;
    }

    public int getScore() {
        return score;
    }
}
