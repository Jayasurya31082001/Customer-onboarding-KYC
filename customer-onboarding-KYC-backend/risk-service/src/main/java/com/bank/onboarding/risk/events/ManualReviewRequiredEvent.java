package com.bank.onboarding.risk.events;

import org.springframework.context.ApplicationEvent;

public class ManualReviewRequiredEvent extends ApplicationEvent {

    private final String customerId;
    private final String customerEmail;
    private final int riskScore;
    private final String reason;

    public ManualReviewRequiredEvent(Object source, String customerId, String customerEmail, 
                                    int riskScore, String reason) {
        super(source);
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.riskScore = riskScore;
        this.reason = reason;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public int getRiskScore() {
        return riskScore;
    }

    public String getReason() {
        return reason;
    }
}
