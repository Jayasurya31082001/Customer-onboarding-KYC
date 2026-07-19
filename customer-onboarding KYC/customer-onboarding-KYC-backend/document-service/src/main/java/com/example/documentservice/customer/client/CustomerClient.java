package com.example.documentservice.customer.client;

import java.util.UUID;

public interface CustomerClient {

    boolean customerExists(UUID customerId);
}
