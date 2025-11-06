import * as yup from "yup";

export interface User {
  id: number;
  email: string;
  firstName: string;
  lastName: string;
  phoneNumber?: string;
  roles: string[];
  createdAt: string;
}

export const loginInputSchema = yup.object({
  email: yup
    .string()
    .email("Invalid email address")
    .required("Email is required"),
  password: yup
    .string()
    .min(6, "Password must be at least 6 characters")
    .required("Password is required"),
});

export type LoginInput = yup.InferType<typeof loginInputSchema>;

export interface AuthResponse {
  token: string;
  type: string;
  expiresIn: number;
  user: User;
  issuedAt: string;
}
