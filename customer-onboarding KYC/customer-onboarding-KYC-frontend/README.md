# Customer Onboarding KYC Frontend

React 18 + TypeScript 5 single-page application for customer onboarding and compliance operations.

## Features

- Multi-step onboarding wizard with reducer-backed state context.
- Step 1 personal details form using `react-hook-form` + `zod` validation.
- Step 2 drag-and-drop document upload using `react-dropzone` with type/size validation and progress UI.
- KYC status polling using React Query (`refetchInterval=5000ms` when pending, `staleTime=4000ms`).
- Protected Compliance Agent Dashboard for `COMPLIANCE_OFFICER` users only.
- Mock login flow (valid email + `Welcome@123`) with one hardcoded compliance admin account.
- Axios API client interceptors for JWT propagation, correlation IDs, and 401 handling.
- React Testing Library tests for login and registration submit flows.

## Scripts

- `npm run dev` starts the Vite development server.
- `npm run build` runs TypeScript build and production bundle.
- `npm run test` runs Vitest + React Testing Library tests.

## Environment Variables

- `VITE_CUSTOMER_API_BASE_URL` default `http://localhost:8081`
- `VITE_DOCUMENT_API_BASE_URL` default `http://localhost:8082`
- `VITE_KYC_API_BASE_URL` default `http://localhost:8083`
- `VITE_RISK_API_BASE_URL` default `http://localhost:8084`
- `VITE_ACCOUNT_API_BASE_URL` default `http://localhost:8085`
- `VITE_KYC_STATUS_ENDPOINT_TEMPLATE` optional placeholder for KYC status endpoint (for example `/api/v1/kyc/:customerId/status`)

If `VITE_KYC_STATUS_ENDPOINT_TEMPLATE` is not configured (or the request fails), the frontend uses a built-in demo fallback that returns `KYC_InProgress` for initial polls and then transitions to a deterministic final status.

## Backend Endpoint Mapping

Mapped from existing Java controllers:

- `POST /api/v1/customers` -> `customer-service` `CustomerController#registerCustomer`
- `GET /api/v1/customers/{customerId}` -> `customer-service` `CustomerController#getCustomer`
- `PATCH /api/v1/customers/{customerId}/status` -> `customer-service` `CustomerController#updateOnboardingStatus`
- `POST /api/documents` -> `document-service` `DocumentController#uploadDocument`
- `GET /api/v1/accounts/customer/{customerId}` -> `account-service` `AccountController#getByCustomerId`

Not found as public endpoints in backend controllers (marked TODO in code):

- Public KYC status polling endpoint
- Compliance queue/list endpoint
- Compliance decision submission endpoint
