import { z } from "zod";

export const guestSchema = z.object({
  type: z.enum(["adult", "child"]),
  firstName: z
    .string()
    .min(2, "First name must be at least 2 characters.")
    .max(50, "First name must be up to 50 characters.")
    .regex(
      /^[A-Za-z'-]+$/,
      "Only Latin letters, hyphens, and apostrophes are allowed."
    ),
  lastName: z
    .string()
    .min(2, "Last name must be at least 2 characters.")
    .max(50, "Last name must be up to 50 characters.")
    .regex(
      /^[A-Za-z'-]+$/,
      "Only Latin letters, hyphens, and apostrophes are allowed."
    ),
});

export const bookingFormSchema = z.object({
  guests: z.array(guestSchema),
});

export type BookingFormData = z.infer<typeof bookingFormSchema>;
