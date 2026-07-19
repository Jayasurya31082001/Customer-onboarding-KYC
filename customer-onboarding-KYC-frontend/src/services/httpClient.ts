import axios, { AxiosError, AxiosHeaders, type AxiosInstance } from "axios";
import { ApiProblemError, type ProblemDetail } from "../types/api.types";
import { getToken, logout } from "./authService";

const CORRELATION_HEADER = "X-Correlation-ID";

let currentCorrelationId: string | null = null;

export const getCorrelationId = (): string | null => currentCorrelationId;

export const ensureCorrelationId = (): string => {
  if (!currentCorrelationId) {
    currentCorrelationId = crypto.randomUUID();
  }
  return currentCorrelationId;
};

const extractProblemDetail = (error: AxiosError<ProblemDetail>): ProblemDetail => {
  const responseData = error.response?.data;
  const fallback: ProblemDetail = {
    title: "Request failed",
    detail: error.message,
    status: error.response?.status,
  };

  if (!responseData || typeof responseData !== "object") {
    return fallback;
  }

  return {
    ...responseData,
    correlationId:
      responseData.correlationId ??
      (error.response?.headers?.["x-correlation-id"] as string | undefined) ??
      getCorrelationId() ??
      undefined,
  };
};

const createConfiguredClient = (baseURL: string): AxiosInstance => {
  const client = axios.create({
    baseURL,
    timeout: 10_000,
  });

  client.interceptors.request.use((config) => {
    const token = getToken();
    const correlationId = ensureCorrelationId();
    const headers = AxiosHeaders.from(config.headers);

    headers.set(CORRELATION_HEADER, correlationId);

    if (token) {
      headers.set("Authorization", `Bearer ${token}`);
    }

    config.headers = headers;

    return config;
  });

  client.interceptors.response.use(
    (response) => {
      const correlationId = response.headers["x-correlation-id"] as string | undefined;
      if (correlationId) {
        currentCorrelationId = correlationId;
      }
      return response;
    },
    (error: AxiosError<ProblemDetail>) => {
      if (error.response?.status === 401) {
        logout();
      }

      const problem = extractProblemDetail(error);
      return Promise.reject(new ApiProblemError(problem));
    },
  );

  return client;
};

export const customerHttpClient = createConfiguredClient(
  import.meta.env.VITE_CUSTOMER_API_BASE_URL ?? "http://localhost:8081",
);

export const documentHttpClient = createConfiguredClient(
  import.meta.env.VITE_DOCUMENT_API_BASE_URL ?? "http://localhost:8082",
);

export const accountHttpClient = createConfiguredClient(
  import.meta.env.VITE_ACCOUNT_API_BASE_URL ?? "http://localhost:8085",
);

export const kycHttpClient = createConfiguredClient(
  import.meta.env.VITE_KYC_API_BASE_URL ?? "http://localhost:8083",
);

export const riskHttpClient = createConfiguredClient(
  import.meta.env.VITE_RISK_API_BASE_URL ?? "http://localhost:8084",
);
