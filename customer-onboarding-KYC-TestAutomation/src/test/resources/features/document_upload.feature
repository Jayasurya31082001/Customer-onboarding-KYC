# ─────────────────────────────────────────────────────────────────────────────
# Feature: Document Upload API
#
# Tests the Document Service POST /api/documents endpoint covering:
#   - 201 Created (valid PDF upload)
#   - 400 Bad Request (unsupported file type)
#   - 400 / 500 (oversized file exceeds 5 MB limit)
#
# All uploads are performed via multipart/form-data RestAssured requests.
# No browser or WebDriver is used.
# ─────────────────────────────────────────────────────────────────────────────
@document-upload
Feature: Document Upload API

  Background:
    Given the Document Service is running on its configured port
    And a registered customer exists for document upload tests

  @smoke @QTEST-2001
  Scenario: A valid PDF document is uploaded successfully
    When a valid PDF file is uploaded for the registered customer
    Then the document upload response status code is 201
    And the upload response contains a document ID
    And the upload response contains the customer ID

  @QTEST-2002
  Scenario: A valid JPEG image is uploaded successfully
    When a valid JPEG file is uploaded for the registered customer
    Then the document upload response status code is 201
    And the upload response contains a document ID

  @QTEST-2003
  Scenario: Uploading an unsupported file type returns 400
    When a file with an unsupported MIME type "application/x-executable" is uploaded for the registered customer
    Then the document upload response status code is 400

  @QTEST-2004
  Scenario: Uploading a file exceeding the 5 MB size limit returns 400
    When an oversized file larger than 5 MB is uploaded for the registered customer
    Then the document upload response status code is 400

  @QTEST-2005
  Scenario: Latest document metadata can be queried by customer ID after upload
    Given a valid PDF has already been uploaded for the registered customer
    When the latest document metadata is queried for the registered customer
    Then the metadata response status code is 200
    And the metadata contains a non-null document ID
    And the metadata content type is "application/pdf"
