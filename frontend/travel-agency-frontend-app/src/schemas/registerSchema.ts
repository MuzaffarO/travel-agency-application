import { z } from "zod";

export const registerSchema = z
  .object({
    firstName: z
      .string()
      .min(2, "First name must be at least 2 characters")
      .max(50, "First name must be up to 50 characters")
      .regex(
        /^[A-Za-z'-]+$/,
        "Only Latin letters, hyphens, and apostrophes are allowed"
      ),
    lastName: z
      .string()
      .min(2, "First name must be at least 2 characters")
      .max(50, "First name must be up to 50 characters")
      .regex(
        /^[A-Za-z'-]+$/,
        "Only Latin letters, hyphens, and apostrophes are allowed"
      ),
    email: z
      .string()
      .email(
        "Invalid email address. Please ensure it follows the format: username@domain.com"
      ),
    password: z
      .string()
      .min(8, "Password must be at least 8 characters")
      .max(16, "Password must be at most 16 characters")
      .regex(/[A-Z]/, "At least one uppercase letter required")
      .regex(/[a-z]/, "At least one lowercase letter required")
      .regex(/\d/, "At least one number required")
      .regex(/[^A-Za-z0-9]/, "At least one special character required"),
    confirmPassword: z.string(),
  })
  .refine((data) => data.password === data.confirmPassword, {
    message: "Confirm password must match your password",
    path: ["confirmPassword"],
  });

export type RegisterFormData = z.infer<typeof registerSchema>;
