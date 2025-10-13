import { z } from "zod";

export const loginSchema = z.object({
  email: z
    .string()
    .nonempty("Email address is required. Please enter your email to continue")
    .email("Invalid email address"),
  password: z
    .string()
    .nonempty("Password is required. Please enter your password to continue")
    .min(6, "Password must be at least 6 characters"),
});

export type LoginFormData = z.infer<typeof loginSchema>;
