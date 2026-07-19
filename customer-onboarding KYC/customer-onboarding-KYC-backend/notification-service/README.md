# Notification Service

## Overview

Notification Service receives internal onboarding events and dispatches customer notifications.
It currently handles account-created and application-rejected events.

## Architecture

- Spring Boot 3 / Java 17 service
- H2 + Liquibase persistence for notification logs and templates
- Internal event ingestion API -> domain events -> dispatch pipeline
- Pluggable sender clients (mock and REST provider)
- Resilience4j circuit breaker and retry for provider calls

Internal event ingress APIs:

- `POST /api/internal/events/account-created`
- `POST /api/internal/events/application-rejected`

## Setup

1. Prerequisites:
   - Java 17+
   - Maven 3.9+
2. Default local port: `8086`
3. Optional external provider:
   - Notification provider on `8091` when `notification-provider.mock-enabled=false`
4. Important properties:
   - `server.port=8086`
   - `notification-provider.base-url=http://localhost:8091`
   - `notification-provider.mock-enabled=true`

Run locally:

```powershell
cd "C:\Users\jayasm\VS code\EDJ\edj_cross_skilling_jayasm\customer-onboarding KYC\Java\notification-service"
mvn spring-boot:run
```

## Usage

Account created event payload:

```json
{
  "customerId": "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
  "customerEmail": "alice.walker@example.com",
  "accountNumber": "12345678",
  "sortCode": "10-20-30"
}
```

Application rejected event payload:

```json
{
  "customerId": "9f0a75ab-f7c6-48d4-a0ac-2c1d8b1df3dd",
  "customerEmail": "alice.walker@example.com",
  "reason": "Risk score exceeded rejection threshold"
}
```

Docs and tests:

- Swagger UI: `http://localhost:8086/swagger-ui.html`
- OpenAPI: `http://localhost:8086/api-docs`
- Tests: `mvn clean test`

## Limitations

- Uses in-memory H2 by default.
- Delivery behavior in local mode can differ when mock sender is enabled.
- Email templates are currently minimal and optimized for internal demos.

## Troubleshooting

- If notifications are not sent, verify payload includes `customerEmail`.
- If provider calls fail, keep `notification-provider.mock-enabled=true` for local flows.
- If events are accepted but no dispatch occurs, inspect notification logs and template keys.
