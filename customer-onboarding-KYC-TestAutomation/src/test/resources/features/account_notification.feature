# ─────────────────────────────────────────────────────────────────────────────
# Feature: Account Provisioning and Notification
#
# Tests the Account Service and Notification Service ingress endpoints,
# verifying that:
#   - A bank account is provisioned when risk disposition is AUTO_APPROVE (KYC VERIFIED)
#   - No account is provisioned and a rejection notification is dispatched
#     when risk disposition is AUTO_REJECT (KYC REJECTED)
# ─────────────────────────────────────────────────────────────────────────────
@account-provisioning @notification
Feature: Account Provisioning and Notification

  Background:
    Given the Account Service is running on its configured port
    And the Notification Service is running on its configured port

  @smoke @QTEST-4001
  Scenario: Account is provisioned when KYC is verified and risk disposition is AUTO_APPROVE
    Given a customer with email "approved.account@example.com" has passed KYC verification
    When a risk-assessed event with disposition "AUTO_APPROVE" and score 20 is sent for that customer
    Then the account ingress event response status code is 202
    And an account record is eventually created for that customer

  @QTEST-4002
  Scenario: No account is provisioned when KYC is rejected and risk disposition is AUTO_REJECT
    Given a customer with email "rejected.account@example.com" has failed KYC verification
    When a risk-assessed event with disposition "AUTO_REJECT" and score 90 is sent for that customer
    Then the account ingress event response status code is 202
    And no account record exists for that customer

  @QTEST-4003
  Scenario: Account-created notification is dispatched after successful account provisioning
    When an account-created notification is sent for customer "notif.approve@example.com" with account number "12345678" and sort code "102030"
    Then the notification service returns status code 202

  @QTEST-4004
  Scenario: Application-rejected notification is dispatched when KYC is rejected
    When an application-rejected notification is sent for customer "notif.rejected@example.com" with reason "KYC identity verification failed"
    Then the notification service returns status code 202

  @QTEST-4005
  Scenario: Account Service query returns 404 for a customer who never had an account provisioned
    Given a random customer ID that has no associated account
    When the account is queried by that customer ID
    Then the account query response status code is 404
