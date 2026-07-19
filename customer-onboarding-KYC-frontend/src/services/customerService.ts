import type {
  CreateCustomerRequest,
  CustomerCreatedResponse,
  CustomerResponse,
  OnboardingStatus,
} from "../types/customer.types";
import { customerHttpClient } from "./httpClient";

interface UpdateCustomerStatusRequest {
  status: OnboardingStatus;
}

export const customerService = {
  async registerCustomer(payload: CreateCustomerRequest): Promise<CustomerCreatedResponse> {
    const response = await customerHttpClient.post<CustomerCreatedResponse>("/api/v1/customers", payload);
    return response.data;
  },

  async getCustomersByStatus(status: OnboardingStatus): Promise<CustomerResponse[]> {
    const response = await customerHttpClient.get<CustomerResponse[]>("/api/v1/customers", {
      params: { status },
    });
    return response.data;
  },

  async getCustomer(customerId: string): Promise<CustomerResponse> {
    const response = await customerHttpClient.get<CustomerResponse>(`/api/v1/customers/${customerId}`);
    return response.data;
  },

  async updateCustomerStatus(customerId: string, status: OnboardingStatus): Promise<CustomerResponse> {
    const payload: UpdateCustomerStatusRequest = { status };
    const response = await customerHttpClient.patch<CustomerResponse>(
      `/api/v1/customers/${customerId}/status`,
      payload,
    );
    return response.data;
  },
};
