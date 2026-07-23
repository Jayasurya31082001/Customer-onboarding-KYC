package com.kyc.automation.client;

import com.kyc.automation.config.BaseApiConfig;
import com.kyc.automation.util.ApiClientUtil;
import com.kyc.automation.util.ValidationUtil;
import io.restassured.response.Response;
import io.restassured.specification.RequestSpecification;

import java.util.Map;

/**
 * API client for the Notification Service (port 8086).
 *
 * <p>Covers notification ingress endpoints:
 * <ul>
 *   <li>POST /api/internal/events/account-created    – triggers account-creation notification email</li>
 *   <li>POST /api/internal/events/application-rejected – triggers rejection notification email</li>
 * </ul>
 *
 * <p>Both endpoints return HTTP 202 Accepted on success.
 */
public class NotificationApiClient {

    private final RequestSpecification spec;

    public NotificationApiClient() {
        this.spec = BaseApiConfig.notificationServiceSpec();
    }

    /**
     * Sends an account-created notification event.
     * Expected response: HTTP 202 Accepted.
     *
     * @param customerId    UUID of the customer
     * @param customerEmail customer e-mail address
     * @param accountNumber newly provisioned account number
     * @param sortCode      newly provisioned sort code
     */
    public Response sendAccountCreatedNotification(String customerId,
                                                   String customerEmail,
                                                   String accountNumber,
                                                   String sortCode) {
        ValidationUtil.requireNonEmpty(customerId, "customerId", "NotificationApiClient.sendAccountCreatedNotification");
        ValidationUtil.requireNonEmpty(customerEmail, "customerEmail", "NotificationApiClient.sendAccountCreatedNotification");
        ValidationUtil.requireNonEmpty(accountNumber, "accountNumber", "NotificationApiClient.sendAccountCreatedNotification");
        ValidationUtil.requireNonEmpty(sortCode, "sortCode", "NotificationApiClient.sendAccountCreatedNotification");

        return ApiClientUtil.executePost(spec, "/api/internal/events/account-created", Map.of(
                "customerId",    customerId,
                "customerEmail", customerEmail,
                "accountNumber", accountNumber,
                "sortCode",      sortCode
        ));
    }

    /**
     * Sends an application-rejected notification event.
     * Expected response: HTTP 202 Accepted.
     *
     * @param customerId    UUID of the customer
     * @param customerEmail customer e-mail address
     * @param reason        human-readable rejection reason
     */
    public Response sendApplicationRejectedNotification(String customerId,
                                                        String customerEmail,
                                                        String reason) {
        ValidationUtil.requireNonEmpty(customerId, "customerId", "NotificationApiClient.sendApplicationRejectedNotification");
        ValidationUtil.requireNonEmpty(customerEmail, "customerEmail", "NotificationApiClient.sendApplicationRejectedNotification");
        ValidationUtil.requireNonEmpty(reason, "reason", "NotificationApiClient.sendApplicationRejectedNotification");

        return ApiClientUtil.executePost(spec, "/api/internal/events/application-rejected", Map.of(
                "customerId",    customerId,
                "customerEmail", customerEmail,
                "reason",        reason
        ));
    }
}
