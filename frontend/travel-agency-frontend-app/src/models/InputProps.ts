import type { ChangeEvent, ReactNode } from "react";

export default interface InputProps {
  id: string;
  label: string;
  type?: "text" | "email" | "password" | "textarea" | "file";
  name: string;
  placeholder?: string;
  helperText?: string | string[] | React.ReactNode;
  value?: string;
  className?: string;
  maxLength?: number;
  error?: string;
  icon?: ReactNode;
  disabled?: boolean;
  onIconClick?: () => void;
  onChange?: (e: ChangeEvent<HTMLInputElement>) => void;
  onKeyDown?: (e: React.KeyboardEvent<HTMLInputElement>) => void;
}
