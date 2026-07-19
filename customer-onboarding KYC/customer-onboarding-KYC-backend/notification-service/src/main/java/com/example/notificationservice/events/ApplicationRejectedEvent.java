package com.example.notificationservice.events;

import org.springframework.context.ApplicationEvent;

public class ApplicationRejectedEvent extends ApplicationEvent {

    private final String customerId;
    private final String customerEmail;
    private final String reason;

    public ApplicationRejectedEvent(Object source, String customerId, String customerEmail, String reason) {
        super(source);
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.reason = reason;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getReason() {
        return reason;
    }
}
