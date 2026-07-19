# Account Service

## Overview

Account Service provisions customer accounts for approved onboarding cases.
It consumes risk-assessed events and creates accounts only for AUTO_APPROVE outcomes.

## Architecture

- Spring Boot 3 / Java 17 service
- H2 + Liquibase persistence for account and audit records
- Event-driven ingestion from Risk Service
- Forwards account-created events to Notification Service
- Updates customer onboarding status to APPROVED in Customer Service
- Supports mock core banking client for local development

Key APIs:

- `GET /api/v1/accounts/customer/{customerId}`
- `POST /api/internal/events/risk-assessed`

## Setup

1. Prerequisites:
   - Java 17+
   - Maven 3.9+
   - Customer Service on `8081`
   - Notification Service on `8086`
2. Optional external dependency:
   - Core banking provider on `8090` when `core-banking.mock-enabled=false`
3. Default local port: `8085`
4. Important properties:
   - `server.port=8085`
   - `customer.service.base-url=http://localhost:8081`
   - `notification.service.base-url=http://localhost:8086`
   - `core-banking.base-url=http://localhost:8090`
   - `core-banking.mock-enabled=true`

Run locally:

```powershell
cd "C:\Users\jayasm\VS code\EDJ\edj_cross_skilling_jayasm\customer-onboarding KYC\Java\account-service"
mvn spring-boot:run
```

## Usage

Risk Service calls `POST /api/internal/events/risk-assessed`.
For `AUTO_APPROVE`:

1. Account is created.
2. Account-created event is forwarded to Notification Service.
3. Customer status is updated to APPROVED.

Query account by customer:

- `GET /api/v1/accounts/customer/{customerId}`

Docs and tests:

- Swagger UI: `http://localhost:8085/swagger-ui.html`
- OpenAPI: `http://localhost:8085/api-docs`
- Tests: `mvn clean test`

## Limitations

- Uses in-memory H2 by default.
- Account provisioning for non-AUTO_APPROVE dispositions is intentionally skipped.
- Full production behavior depends on external core banking integration.

## Troubleshooting

- If no account is created, verify disposition is `AUTO_APPROVE`.
- If account exists but status is not APPROVED, check Customer Service internal event endpoint availability.
- If notifications are missing, verify Notification Service ingress endpoint and payload compatibility.
