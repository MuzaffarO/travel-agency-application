import { useState } from "react";
import { useForm, FormProvider } from "react-hook-form";
import Input from "../ui/Input";
import Button from "../components/Button";
import { updatePassword, type UpdatePasswordRequest } from "../services/updatePassword";
import { Check, X } from "lucide-react";

type ChangePasswordForm = {
  currentPassword: string;
  newPassword: string;
  confirmPassword: string;
};

export default function ChangePasswordPage() {
  const [error, setError] = useState<string | null>(null);
  const [success, setSuccess] = useState<string | null>(null);
  const [loading, setLoading] = useState(false);

  const methods = useForm<ChangePasswordForm>({
    defaultValues: {
      currentPassword: "",
      newPassword: "",
      confirmPassword: "",
    },
    mode: "onSubmit",
  });

  const onSubmit = async (data: ChangePasswordForm) => {
    const savedUser = localStorage.getItem("user");
    if (!savedUser) {
      setError("User not found");
      return;
    }

    const { token, email } = JSON.parse(savedUser);
    setError(null);
    setSuccess(null);

    // Validation
    if (data.newPassword !== data.confirmPassword) {
      setError("New password and confirm password do not match");
      return;
    }

    if (data.currentPassword === data.newPassword) {
      setError("New password must be different from current password");
      return;
    }

    // Password validation: at least 8 characters, uppercase, lowercase, number, special character
    const passwordRegex = /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/;
    if (!passwordRegex.test(data.newPassword)) {
      setError(
        "New password must be at least 8 characters and contain uppercase, lowercase, number, and special character (@$!%*?&)"
      );
      return;
    }

    setLoading(true);
    try {
      const updateData: UpdatePasswordRequest = {
        currentPassword: data.currentPassword,
        newPassword: data.newPassword,
      };

      await updatePassword(token, email, updateData);
      setSuccess("Password changed successfully");
      methods.reset();
    } catch (err: unknown) {
      console.error("Failed to update password:", err);
      if (err instanceof Error) {
        setError(err.message || "Failed to update password");
      } else {
        setError("Failed to update password");
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="bg-white w-full rounded-lg shadow p-6 max-w-xl">
      <h3 className="h2 text-blue-09 mb-6">Change password</h3>

      {error && (
        <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded flex items-center gap-2">
          <X className="w-4 h-4" />
          {error}
        </div>
      )}

      {success && (
        <div className="mb-4 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded flex items-center gap-2">
          <Check className="w-4 h-4" />
          {success}
        </div>
      )}

      <FormProvider {...methods}>
        <form onSubmit={methods.handleSubmit(onSubmit)} className="space-y-4">
          <Input
            {...methods.register("currentPassword", {
              required: "Current password is required",
            })}
            type="password"
            id="currentPassword"
            name="currentPassword"
            label="Current password"
            placeholder="Enter current password"
            error={methods.formState.errors.currentPassword?.message}
          />

          <Input
            {...methods.register("newPassword", {
              required: "New password is required",
              minLength: {
                value: 8,
                message: "Password must be at least 8 characters",
              },
              pattern: {
                value: /^(?=.*[a-z])(?=.*[A-Z])(?=.*\d)(?=.*[@$!%*?&])[A-Za-z\d@$!%*?&]{8,}$/,
                message:
                  "Password must contain uppercase, lowercase, number, and special character (@$!%*?&)",
              },
            })}
            type="password"
            id="newPassword"
            name="newPassword"
            label="New password"
            placeholder="Enter new password"
            error={methods.formState.errors.newPassword?.message}
            helperText="Must be at least 8 characters with uppercase, lowercase, number, and special character"
          />

          <Input
            {...methods.register("confirmPassword", {
              required: "Please confirm your new password",
              validate: (value) => {
                const newPassword = methods.getValues("newPassword");
                return value === newPassword || "Passwords do not match";
              },
            })}
            type="password"
            id="confirmPassword"
            name="confirmPassword"
            label="Confirm new password"
            placeholder="Confirm new password"
            error={methods.formState.errors.confirmPassword?.message}
          />

          <div className="flex justify-end gap-3 pt-4">
            <Button
              type="button"
              variant="secondary"
              size="medium"
              onClick={() => {
                methods.reset();
                setError(null);
                setSuccess(null);
              }}
              disabled={loading}>
              Cancel
            </Button>
            <Button type="submit" variant="primary" size="medium" disabled={loading}>
              {loading ? "Updating..." : "Change password"}
            </Button>
          </div>
        </form>
      </FormProvider>
    </div>
  );
}

