import { FormProvider, useForm } from "react-hook-form";
import { zodResolver } from "@hookform/resolvers/zod";
import Input from "../ui/Input";
import PasswordInput from "../ui/PasswordInput";
import Button from "../components/Button";
import { Link, useNavigate } from "react-router";
import {
  forgotPasswordSchema,
  resetPasswordSchema,
  type ForgotPasswordFormData,
  type ResetPasswordFormData,
} from "../schemas/forgotPasswordSchema";
import { useEffect, useState, useRef } from "react";
import { passwordRules } from "../schemas/passwordRules";
import SuccessToast from "../ui/SuccessToast";
import toast from "react-hot-toast";

const ForgotPasswordPage = () => {
  // WILL REPLACE WITH ACTUAL BACKEND LOGIC
  const MOCK_CODE = "123456";

  const [step, setStep] = useState<"email" | "verify" | "done">("email");
  const [submittedEmail, setSubmittedEmail] = useState<string | null>(null);
  const [timer, setTimer] = useState<number>(60);
  const [code, setCode] = useState<string>("");
  const intervalRef = useRef<NodeJS.Timeout | null>(null);
  const navigate = useNavigate();
  const emailMethods = useForm<ForgotPasswordFormData>({
    resolver: zodResolver(forgotPasswordSchema),
    defaultValues: { email: "" },
    mode: "onChange",
  });

  const passwordMethods = useForm<ResetPasswordFormData>({
    resolver: zodResolver(resetPasswordSchema),
    defaultValues: { newPassword: "", confirmNewPassword: "" },
    mode: "onChange",
  });

  const onSubmitEmail = (data: ForgotPasswordFormData) => {
    setSubmittedEmail(data.email);
    setStep("verify");
    setTimer(60);
  };

  const onSubmitCode = (formData: { code: string }) => {
    if (formData.code === MOCK_CODE) {
      setStep("done");
    } else {
      alert("Incorrect code. Try again.");
    }
  };

  const onSubmitPassword = (data: ResetPasswordFormData) => {
    console.log("New password:", data.newPassword);
    toast.custom(
      (t) => (
        <SuccessToast
          t={t}
          title="Success"
          message="Your password has been successfully changed."
        />
      ),
      { duration: 5000 }
    );
    navigate("/login");
  };

  const resendCode = (email: string) => {
    console.log("Resend code to:", email);
    alert(`Mock: code ${MOCK_CODE} sent to ${email}`);
  };

  useEffect(() => {
    if (step !== "verify") return;

    if (intervalRef.current) clearInterval(intervalRef.current);

    intervalRef.current = setInterval(() => {
      setTimer((prev) => prev - 1);
    }, 1000);

    return () => {
      if (intervalRef.current) clearInterval(intervalRef.current);
    };
  }, [step]);

  useEffect(() => {
    if (timer === 0 && submittedEmail && step === "verify") {
      resendCode(submittedEmail);
      setTimer(60);
    }
  }, [timer, submittedEmail, step]);

  useEffect(() => {
    if (step === "done" && intervalRef.current) {
      clearInterval(intervalRef.current);
    }
  }, [step]);

  return (
    <>
      {step === "email" && (
        <FormProvider {...emailMethods}>
          <form
            className="flex flex-col gap-4"
            onSubmit={emailMethods.handleSubmit(onSubmitEmail)}
          >
            <h1 className="text-2xl font-bold mb-6">Reset password</h1>
            <Input
              {...emailMethods.register("email")}
              id="email"
              name="email"
              label="Email"
              type="text"
              placeholder="Enter your email"
              error={emailMethods.formState.errors.email?.message}
              helperText="e.g. username@domain.com"
            />
            <Button disabled={!emailMethods.formState.isValid} className="mt-6">
              Request a reset link
            </Button>
            <p className="text-xs">
              Don’t have an account?{" "}
              <Link to="/register">
                <span className="underline text-blue-05">
                  Create an account
                </span>
              </Link>
            </p>
          </form>
        </FormProvider>
      )}

      {step === "verify" && (
        <form
          className="flex flex-col gap-4"
          onSubmit={(e) => {
            e.preventDefault();
            const formData = new FormData(e.currentTarget);
            onSubmitCode({ code: formData.get("code") as string });
          }}
        >
          <h1 className="text-2xl font-bold mb-6">Enter verification code</h1>
          <p className="text-sm text-gray-700">
            The verification code has been sent to{" "}
            <span className="font-medium">{submittedEmail}</span>.
          </p>
          <Input
            id="code"
            name="code"
            label="Verification Code"
            type="text"
            value={code}
            onChange={(e) => {
              const onlyDigits = e.target.value.replace(/\D/g, "");
              if (onlyDigits.length <= 6) {
                setCode(onlyDigits);
              }
            }}
            maxLength={6}
            placeholder="Enter verification code"
            helperText={
              timer > 0
                ? `Not received yet? Resend in ${timer} seconds`
                : "Sending new code..."
            }
          />
          <Button disabled={code.length !== 6} className="mt-6">
            Continue
          </Button>
        </form>
      )}

      {step === "done" && (
        <FormProvider {...passwordMethods}>
          <form
            className="flex flex-col gap-4"
            onSubmit={passwordMethods.handleSubmit(onSubmitPassword)}
          >
            <h1 className="text-2xl font-bold mb-6">Set new password</h1>
            <PasswordInput
              {...passwordMethods.register("newPassword")}
              id="newPassword"
              name="newPassword"
              label="New Password"
              placeholder="Enter your new password"
              rules={passwordRules}
            />

            <PasswordInput
              {...passwordMethods.register("confirmNewPassword")}
              name="confirmNewPassword"
              id="confirmNewPassword"
              label="Confirm New Password"
              placeholder="Confirm your new password"
              match={passwordMethods.watch("newPassword")}
              helperText="Confirm password must match your password"
            />
            <Button
              disabled={!passwordMethods.formState.isValid}
              className="mt-6"
            >
              Reset
            </Button>
          </form>
        </FormProvider>
      )}
    </>
  );
};

export default ForgotPasswordPage;
