# ─────────────────────────────────────────────────────────────────────────────
# Feature: Compliance Risk Scoring
#
# Tests the Risk Service ingress and query endpoints verifying that:
#   - A LOW risk score results in Disposition = AUTO_APPROVE (auto-approve)
#   - A HIGH-RISK nationality (IR, KP, SY) results in Disposition = MANUAL_REVIEW
#   - A failed KYC (Fail status) results in Disposition = AUTO_REJECT
#
# Risk scoring uses nationalities defined in risk-service application.yml:
#   high-risk-nationalities: [IR, KP, SY]
# ─────────────────────────────────────────────────────────────────────────────
@compliance @risk-scoring
Feature: Compliance Risk Scoring

  Background:
    Given the Risk Service is running on its configured port

  @smoke @QTEST-5001
  Scenario: A clean customer profile with a low risk score is auto-approved
    When a KYC-completed event is sent to the risk service for customer "low.risk.customer@example.com" with:
      | kycStatus      | Pass |
      | nationality    | GB   |
      | pepMatch       | false|
      | sanctionsMatch | false|
    Then the risk event ingress response status code is 202
    And the latest risk assessment for that customer has disposition "AUTO_APPROVE"

  @QTEST-5002
  Scenario: A customer from a high-risk nationality is flagged for manual review
    When a KYC-completed event is sent to the risk service for customer "high.risk.customer@example.com" with:
      | kycStatus      | Pass |
      | nationality    | IR   |
      | pepMatch       | false|
      | sanctionsMatch | false|
    Then the risk event ingress response status code is 202
    And the latest risk assessment for that customer has disposition "MANUAL_REVIEW"

  @QTEST-5003
  Scenario: A customer who is a PEP match is flagged for manual review
    When a KYC-completed event is sent to the risk service for customer "pep.match.customer@example.com" with:
      | kycStatus      | Pass |
      | nationality    | GB   |
      | pepMatch       | true |
      | sanctionsMatch | false|
    Then the risk event ingress response status code is 202
    And the latest risk assessment for that customer has disposition "MANUAL_REVIEW"

  @QTEST-5004
  Scenario: A customer matching a sanctions list is auto-rejected
    When a KYC-completed event is sent to the risk service for customer "sanctions.customer@example.com" with:
      | kycStatus      | Pass |
      | nationality    | GB   |
      | pepMatch       | false|
      | sanctionsMatch | true |
    Then the risk event ingress response status code is 202
    And the latest risk assessment for that customer has disposition "AUTO_REJECT"

  @QTEST-5005
  Scenario: A customer with a failed KYC outcome is auto-rejected regardless of risk score
    When a KYC-completed event is sent to the risk service for customer "failed.kyc.customer@example.com" with:
      | kycStatus      | Fail |
      | nationality    | GB   |
      | pepMatch       | false|
      | sanctionsMatch | false|
    Then the risk event ingress response status code is 202
    And the latest risk assessment for that customer has disposition "AUTO_REJECT"

  @QTEST-5006
  Scenario: Risk assessment query returns 404 for an unknown customer
    When the latest risk assessment is queried for an unknown customer ID
    Then the risk assessment query response status code is 404
