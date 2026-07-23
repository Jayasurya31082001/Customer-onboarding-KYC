# Document Service

## Overview

Document Service manages KYC document upload and retrieval.
It validates customer existence and publishes document-uploaded events used by KYC orchestration.

## Architecture

- Spring Boot 3 / Java 17 service with multipart upload support
- H2 + Liquibase for local persistence
- Correlation ID propagation with `X-Correlation-ID`
- Asynchronous event forwarding to KYC Service

Key APIs:

- `POST /api/documents` (multipart upload)
- `GET /api/documents/{id}`

## Setup

1. Prerequisites:
   - Java 17+
   - Maven 3.9+
   - Customer Service running on `8081`
   - KYC Service running on `8083`
2. Default local port: `8082`
3. Important properties:
   - `server.port=8082`
   - `spring.servlet.multipart.max-file-size=5MB`
   - `customer.service.base-url=http://localhost:8081`
   - `kyc.service.base-url=http://localhost:8083`

Run locally:

```powershell
cd "C:\Users\jayasm\VS code\EDJ\edj_cross_skilling_jayasm\customer-onboarding KYC\Java\document-service"
mvn spring-boot:run
```

## Usage

Upload endpoint accepts document metadata and file content in multipart form data.

Docs and tests:

- Swagger UI: `http://localhost:8082/swagger-ui.html`
- OpenAPI: `http://localhost:8082/api-docs`
- Tests: `mvn clean test`

## Limitations

- Storage is in-memory H2 by default and not durable across restarts.
- Max upload size is capped at 5 MB unless reconfigured.
- Depends on Customer Service and KYC Service availability for full flow execution.

## Troubleshooting

- `400` on upload commonly indicates invalid multipart form fields.
- `404` or validation failures may mean the customer ID does not exist in Customer Service.
- If uploads succeed but KYC is not triggered, verify `kyc.service.base-url` and KYC ingress endpoint availability.
