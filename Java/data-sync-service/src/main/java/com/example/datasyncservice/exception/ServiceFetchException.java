package com.example.datasyncservice.exception;

/**
 * Thrown when a REST call to a business microservice's {@code /internal/sync}
 * endpoint fails after all retry attempts are exhausted.
 *
 * <p>Causes the current Step to fail. The sync cursor is not advanced,
 * so the next scheduled run retries from the same point.
 */
public class ServiceFetchException extends RuntimeException {

    private final String serviceName;

    public ServiceFetchException(String serviceName, String message) {
        super(message);
        this.serviceName = serviceName;
    }

    public ServiceFetchException(String serviceName, String message, Throwable cause) {
        super(message, cause);
        this.serviceName = serviceName;
    }

    public String getServiceName() {
        return serviceName;
    }
}
