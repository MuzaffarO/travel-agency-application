import { type PasswordRule } from "../ui/PasswordInput/PasswordInput";
export const passwordRules: PasswordRule[] = [
  {
    id: "uppercase",
    label: "At least one uppercase letter required",
    test: (val: string) => /[A-Z]/.test(val),
  },
  {
    id: "lowercase",
    label: "At least one lowercase letter required",
    test: (val: string) => /[a-z]/.test(val),
  },
  {
    id: "number",
    label: "At least one number required",
    test: (val: string) => /[0-9]/.test(val),
  },
  {
    id: "special",
    label: "At least one special character required",
    test: (val: string) => /[!@#$%^&~*/(),.=+_№?":{}|<>]/.test(val),
  },
  {
    id: "length",
    label: "Password must be 8-16 characters long",
    test: (val: string) => val.length >= 8 && val.length <= 16,
  },
];
