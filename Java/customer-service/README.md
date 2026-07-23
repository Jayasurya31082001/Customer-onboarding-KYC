# Customer Service

## Overview

Customer Service owns customer profile data and onboarding status lifecycle.
It is the public entry point for customer registration and status tracking in the KYC onboarding journey.

## Architecture

- Spring Boot 3 / Java 17 service with layered controller-service-repository design
- H2 + Liquibase for local persistence
- Publishes and forwards customer registration events to KYC Service

Key APIs:

- `POST /api/v1/customers`
- `GET /api/v1/customers/{customerId}`
- `PATCH /api/v1/customers/{customerId}/status`
- `POST /api/internal/events/manual-approval-required`
- `POST /api/internal/events/application-rejected`
- `POST /api/internal/events/account-approved`

## Setup

1. Prerequisites:
   - Java 17+
   - Maven 3.9+
2. Default local port: `8081`
3. Important properties:
   - `server.port=8081`
   - `spring.datasource.url=jdbc:h2:mem:customerdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE`
   - `kyc.service.base-url=http://localhost:8083`

Run locally:

```powershell
cd "C:\Users\jayasm\VS code\EDJ\edj_cross_skilling_jayasm\customer-onboarding KYC\Java\customer-service"
mvn spring-boot:run
```

## Usage

Register a customer:

```json
{
  "firstName": "Alice",
  "lastName": "Walker",
  "email": "alice.walker@example.com",
  "dateOfBirth": "1990-02-14",
  "phoneNumber": "+447911123456",
  "nationality": "GB",
  "addressLine1": "221B Baker Street",
  "city": "London",
  "postcode": "SW1A 1AA"
}
```

Update onboarding status:

```json
{
  "status": "KYC_COMPLETED"
}
```

Docs and tests:

- Swagger UI: `http://localhost:8081/swagger-ui.html`
- OpenAPI: `http://localhost:8081/api-docs`
- Tests: `mvn clean verify`

## Limitations

- Uses in-memory H2 by default and is not production-grade persistence.
- Authentication and authorization are not enforced in this service.
- Event propagation is HTTP based and depends on downstream availability.

## Troubleshooting

- `409 Conflict` during registration usually indicates duplicate email.
- If KYC does not start, verify `kyc.service.base-url` and KYC Service health.
- If schema errors occur at startup, run `mvn clean test` to validate Liquibase migrations.
