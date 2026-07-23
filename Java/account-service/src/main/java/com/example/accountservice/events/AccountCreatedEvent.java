package com.example.accountservice.events;

import org.springframework.context.ApplicationEvent;

public class AccountCreatedEvent extends ApplicationEvent {

    private final String customerId;
    private final String customerEmail;
    private final String accountNumber;
    private final String sortCode;

    public AccountCreatedEvent(Object source, String customerId, String customerEmail, 
                               String accountNumber, String sortCode) {
        super(source);
        this.customerId = customerId;
        this.customerEmail = customerEmail;
        this.accountNumber = accountNumber;
        this.sortCode = sortCode;
    }

    public String getCustomerId() {
        return customerId;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getAccountNumber() {
        return accountNumber;
    }

    public String getSortCode() {
        return sortCode;
    }
}
