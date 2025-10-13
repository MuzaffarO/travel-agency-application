import type {
  SubmitHandler,
  UseFormHandleSubmit,
  FieldValues,
} from "react-hook-form";

export interface FormProps<T extends FieldValues> {
  children: React.ReactNode;
  onSubmit: UseFormHandleSubmit<T>;
  onSuccess: SubmitHandler<T>;
  className?: string;
}
