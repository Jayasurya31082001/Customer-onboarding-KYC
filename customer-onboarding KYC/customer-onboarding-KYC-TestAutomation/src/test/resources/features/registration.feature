# ─────────────────────────────────────────────────────────────────────────────
# Feature: Customer Registration API
#
# Tests the Customer Service POST /api/v1/customers endpoint covering:
#   - 201 Created (success)
#   - 400 Bad Request (validation errors)
#   - 409 Conflict (duplicate e-mail)
#
# QTest trace IDs are PLACEHOLDER values — replace with actual QTest IDs.
# Assumption: @QTEST-<id> tags map 1-to-1 with QTest test case IDs.
# ─────────────────────────────────────────────────────────────────────────────
@registration
Feature: Customer Registration API

  Background:
    Given the Customer Service is running on its configured port

  @smoke @QTEST-1001
  Scenario Outline: Customer registration returns the expected HTTP status
    When a customer registration is submitted with:
      | firstName   | <firstName>   |
      | lastName    | <lastName>    |
      | email       | <email>       |
      | dateOfBirth | <dateOfBirth> |
      | phoneNumber | <phoneNumber> |
      | nationality | <nationality> |
      | addressLine1| <addressLine1>|
      | city        | <city>        |
      | postcode    | <postcode>    |
    Then the registration response status code is <expectedStatus>
    And the registration response body contains "<expectedField>" equal to "<expectedValue>"

    Examples:
      | firstName | lastName | email                        | dateOfBirth | phoneNumber    | nationality | addressLine1        | city   | postcode | expectedStatus | expectedField | expectedValue |
      | Alice     | Walker   | alice.walker.reg@example.com | 1990-06-15  | +447911123456  | GB          | 221B Baker Street   | London | NW1 6XE  | 201            | status        | PENDING       |
      | B         | Walker   | bad.firstname@example.com    | 1990-06-15  | +447911123456  | GB          | 221B Baker Street   | London | NW1 6XE  | 400            | errors[0]     | firstName     |
      | Alice     | Walker   | not-an-email                 | 1990-06-15  | +447911123456  | GB          | 221B Baker Street   | London | NW1 6XE  | 400            | errors[0]     | email         |
      | Alice     | Walker   | underage@example.com         | 2015-01-01  | +447911123456  | GB          | 221B Baker Street   | London | NW1 6XE  | 400            | errors[0]     | dateOfBirth   |
      | Alice     | Walker   | bad.phone@example.com        | 1990-06-15  | 07911123456    | GB          | 221B Baker Street   | London | NW1 6XE  | 400            | errors[0]     | phoneNumber   |
      | Alice     | Walker   | bad.postcode@example.com     | 1990-06-15  | +447911123456  | GB          | 221B Baker Street   | London | ZZ99     | 400            | errors[0]     | postcode      |

  @QTEST-1002
  Scenario: Registering an already-registered e-mail returns 409 Conflict
    Given a customer is already registered with email "dup.customer.conflict@example.com"
    When the same email "dup.customer.conflict@example.com" is submitted for registration again
    Then the registration response status code is 409
    And the registration response body contains the conflict error message

  @QTEST-1003
  Scenario: Successfully registered customer is retrievable by their generated ID
    Given a customer registration succeeds with email "retrieve.me@example.com"
    When the customer is fetched by their generated customer ID
    Then the fetch response status code is 200
    And the fetched customer email is "retrieve.me@example.com"
    And the fetched customer onboarding status is "PENDING"
