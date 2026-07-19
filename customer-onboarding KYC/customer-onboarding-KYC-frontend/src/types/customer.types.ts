export enum OnboardingStatus {
  PENDING = "PENDING",
  KYC_IN_PROGRESS = "KYC_IN_PROGRESS",
  KYC_COMPLETED = "KYC_COMPLETED",
  APPROVED = "APPROVED",
  MANUAL_APPROVAL_REQUIRED = "MANUAL_APPROVAL_REQUIRED",
  REJECTED = "REJECTED",
}

export interface PersonalDetailsForm {
  firstName: string;
  lastName: string;
  email: string;
  dateOfBirth: string;
  phoneNumber: string;
  nationality: string;
  addressLine1: string;
  city: string;
  postcode: string;
}

export interface CreateCustomerRequest extends PersonalDetailsForm {}

export interface CustomerCreatedResponse {
  customerId: string;
  status: OnboardingStatus;
  createdAt: string;
}

export interface CustomerResponse {
  customerId: string;
  firstName: string;
  lastName: string;
  email: string;
  dateOfBirth: string;
  phoneNumber: string;
  nationality: string;
  addressLine1: string;
  city: string;
  postcode: string;
  status: OnboardingStatus;
  createdAt: string;
}
