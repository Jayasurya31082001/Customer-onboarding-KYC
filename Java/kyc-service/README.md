# KYC Service

## Overview

KYC Service orchestrates KYC validation once customer registration and document upload events are both available.
It publishes pass or reject outcomes and updates onboarding status.

## Architecture

- Spring Boot 3 / Java 17 event-driven service
- Trigger-state coordination for `customer-registered` and `document-uploaded`
- Async validation flow with Spring application events
- Integrations with Customer, Risk, Document, and Notification services

Internal event ingress APIs:

- `POST /api/internal/events/customer-registered`
- `POST /api/internal/events/document-uploaded`

## Setup

1. Prerequisites:
   - Java 17+
   - Maven 3.9+
   - Customer Service on `8081`
   - Document Service on `8082`
   - Risk Service on `8084`
   - Notification Service on `8086`
2. Default local port: `8083`
3. Important properties:
   - `server.port=8083`
   - `customer.service.base-url=http://localhost:8081`
   - `document.service.base-url=http://localhost:8082`
   - `risk.service.base-url=http://localhost:8084`
   - `notification.service.base-url=http://localhost:8086`

Run locally:

```powershell
cd "C:\Users\jayasm\VS code\EDJ\edj_cross_skilling_jayasm\customer-onboarding KYC\Java\kyc-service"
mvn spring-boot:run
```

## Usage

Typical flow:

1. Receive customer registration event.
2. Receive document upload event.
3. Start async KYC validation when both events are present.
4. On pass, update customer status and publish to Risk Service.
5. On reject, publish rejection event for notification handling.

Docs and tests:

- Swagger UI: `http://localhost:8083/swagger-ui.html`
- OpenAPI: `http://localhost:8083/api-docs`
- Tests: `mvn clean verify`

## Limitations

- Uses in-memory H2 by default.
- End-to-end progression depends on all downstream services being reachable.
- Validation quality depends on upstream payload completeness and data quality.

## Troubleshooting

- If KYC never starts, verify both ingress events are arriving with the same correlation context.
- If pass cases do not reach Risk Service, verify `risk.service.base-url` and Risk ingress availability.
- If customer status does not update, check Customer Service internal status endpoint health.
