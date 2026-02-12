export interface User {
  id: string;
  name: string;
  email: string;
  role: "CLIENT" | "SELLER";
  avatarUrl?: string;
}

export interface SignupRequest {
  name: string;
  email: string;
  password: string;
  role: "CLIENT" | "SELLER";
}

export interface SigninRequest {
  email: string;
  password: string;
}

/**
 * Authentication response from the API.
 */
export interface AuthResponse {
  token: string;
  expiresIn: number;
}
