package com.example.accountservice.service;

import com.example.accountservice.model.Account;

public interface AccountProvisioningService {

    Account createAccountForCustomer(String customerId, String correlationId);
}
