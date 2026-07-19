export enum UserRole {
  CUSTOMER = "CUSTOMER",
  COMPLIANCE_OFFICER = "COMPLIANCE_OFFICER",
}

export interface User {
  id: string;
  email: string;
  roles: UserRole[];
}

export interface AuthSession {
  token: string;
  user: User;
}
