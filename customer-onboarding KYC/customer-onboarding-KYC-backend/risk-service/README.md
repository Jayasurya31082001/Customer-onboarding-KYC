# Risk Service

## Overview

Risk Service consumes KYC completion events, computes risk score and disposition, and orchestrates downstream actions.
It is responsible for the decision point that drives auto-approve, manual review, or reject flows.

## Architecture

- Spring Boot 3 / Java 17 service
- H2 + Liquibase persistence for risk assessments
- Event-driven processing with async forwarders
- Downstream forwarding:
  - AUTO_APPROVE -> Account Service
  - MANUAL review -> Customer Service status update
  - REJECT -> Customer Service status update + Notification Service

Internal event ingress API:

- `POST /api/internal/events/kyc-completed`

## Setup

1. Prerequisites:
   - Java 17+
   - Maven 3.9+
   - Customer Service on `8081`
   - Account Service on `8085`
   - Notification Service on `8086`
2. Default local port: `8084`
3. Important properties:
   - `server.port=8084`
   - `customer.service.base-url=http://localhost:8081`
   - `account.service.base-url=http://localhost:8085`
   - `notification.service.base-url=http://localhost:8086`
   - `risk.high-risk-nationalities=IR,KP,SY`

Run locally:

```powershell
cd "C:\Users\jayasm\VS code\EDJ\edj_cross_skilling_jayasm\customer-onboarding KYC\Java\risk-service"
mvn spring-boot:run
```

## Usage

Send KYC completion event payload with core and enrichment fields:

- Core: `kycCaseId`, `customerId`, `documentId`, `status`, `occurredAt`, `correlationId`
- Enrichment: `nationality`, `dateOfBirth`, `pepMatch`, `sanctionsMatch`, `customerEmail`

Docs and tests:

- Swagger UI: `http://localhost:8084/swagger-ui.html`
- OpenAPI: `http://localhost:8084/v3/api-docs`
- Tests: `mvn clean test`

## Limitations

- Uses in-memory H2 by default.
- Risk model is rule based and intentionally simple for training/demo use.
- Downstream failures can delay cross-service completion despite retries.

## Troubleshooting

- If account creation never starts, verify AUTO_APPROVE dispositions and Account Service connectivity.
- If rejection emails are missing, verify Notification Service ingress endpoint availability.
- If scoring looks incorrect, verify enrichment data is present in incoming KYC-completed payloads.
