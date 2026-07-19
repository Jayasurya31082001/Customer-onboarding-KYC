import { accountHttpClient } from "./httpClient";

export interface AccountResponse {
  accountId: string;
  customerId: string;
  accountNumber: string;
  sortCode: string;
  accountType: string;
  status: string;
  createdAt: string;
}

export const accountService = {
  async getAccountByCustomer(customerId: string): Promise<AccountResponse> {
    const response = await accountHttpClient.get<AccountResponse>(`/api/v1/accounts/customer/${customerId}`);
    return response.data;
  },
};
