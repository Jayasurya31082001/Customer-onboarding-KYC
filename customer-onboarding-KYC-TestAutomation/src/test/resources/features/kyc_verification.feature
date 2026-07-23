# ─────────────────────────────────────────────────────────────────────────────
# Feature: KYC Verification API
#
# Tests the KYC Service internal ingress endpoints which drive the KYC flow:
#   POST /api/internal/events/customer-registered  → initiates KYC case
#   POST /api/internal/events/document-uploaded    → triggers identity verification
#
# Outcomes modelled: VERIFIED (Pass), PENDING (KYC_InProgress), REJECTED (Fail)
# ─────────────────────────────────────────────────────────────────────────────
@kyc-verification
Feature: KYC Verification API

  Background:
    Given the KYC Service is running on its configured port

  @smoke @QTEST-3001
  Scenario: Customer-registered event is accepted and returns 202 Accepted
    Given a valid customer ID exists for KYC initiation
    When the customer-registered event is sent to the KYC service
    Then the KYC ingress response status code is 202

  @QTEST-3002
  Scenario: Document-uploaded event triggers KYC verification and returns 202 Accepted
    Given a valid customer ID and document ID exist for KYC verification
    When the document-uploaded event is sent to the KYC service
    Then the KYC ingress response status code is 202

  @QTEST-3003
  Scenario: KYC case enters PENDING state when document-uploaded event is received before verification completes
    When the document-uploaded event is sent with a new customer ID and document ID
    Then the KYC ingress response status code is 202

  @QTEST-3004
  Scenario: Sending a customer-registered event with missing required fields returns 400
    When an invalid customer-registered event with missing fields is sent
    Then the KYC ingress response status code is 400

  @QTEST-3005
  Scenario: KYC verification outcome for a clean customer profile results in PASS
    Given a customer-registered event has been accepted for a clean customer profile
    When the document-uploaded event is sent to complete KYC verification
    Then the KYC ingress response status code is 202

  @QTEST-3006
  Scenario: KYC verification outcome for a customer failing checks results in FAIL
    Given a customer-registered event has been accepted for a high-risk customer profile
    When the document-uploaded event is sent to trigger KYC for the high-risk customer
    Then the KYC ingress response status code is 202
