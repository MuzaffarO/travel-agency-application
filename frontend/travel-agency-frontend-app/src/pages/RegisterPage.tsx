import { useForm, FormProvider } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";
import { zodResolver } from "@hookform/resolvers/zod";
import Input from "../ui/Input";
import PasswordInput from "../ui/PasswordInput";
import {
  registerSchema,
  type RegisterFormData,
} from "../schemas/registerSchema";
import { passwordRules } from "../schemas/passwordRules";
import Button from "../components/Button";
import { signUp } from "../store/user/userThunks";
import { toast } from "react-hot-toast";
import SuccessToast from "../ui/SuccessToast";
import { useAppDispatch } from "../store/hooks/useAppDispatch";

const RegisterPage = () => {
  const methods = useForm<RegisterFormData>({
    resolver: zodResolver(registerSchema),
    mode: "onSubmit",
  });

  const { watch, setError } = methods;
  const values = watch();
  const allFilled = Object.values(values).every(
    (value) => value && value.trim() !== ""
  );

  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const onSubmit = async (data: RegisterFormData) => {
    const resultAction = await dispatch(
      signUp({
        firstName: data.firstName,
        lastName: data.lastName,
        email: data.email,
        password: data.password,
      })
    );

    if (signUp.fulfilled.match(resultAction)) {
      toast.custom(
        (t) => (
          <SuccessToast
            t={t}
            message="Your account has been created successfully. Please sign in with your details."
            title="Congratulations"
          />
        ),
        {
          duration: 5000,
        }
      );

      navigate("/login");
    }

    if (signUp.rejected.match(resultAction)) {
      const errorMessage = resultAction.payload as string;

      if (errorMessage.includes("Email already exists")) {
        setError("email", {
          type: "manual",
          message: "Email already exists",
        });
      } else {
        toast.error(errorMessage || "Failed to register. Please try again.");
      }
    }
  };

  return (
    <>
      <p className="text-sm">LET'S GET YOU STARTED</p>
      <h1 className="text-6 font-bold">Create an account</h1>

      <FormProvider {...methods}>
        <form
          onSubmit={methods.handleSubmit(onSubmit)}
          className="flex flex-col gap-4 mt-10"
        >
          <div className="flex flex-col lg:flex-row gap-4">
            <Input
              {...methods.register("firstName")}
              id="firstName"
              name="firstName"
              label="First name"
              placeholder="Enter your first name"
              error={methods.formState.errors.firstName?.message}
              helperText="e.g. Johnson"
            />
            <Input
              {...methods.register("lastName")}
              name="lastName"
              id="lastName"
              label="Last name"
              placeholder="Enter your last name"
              error={methods.formState.errors.lastName?.message}
              helperText="e.g. Doe"
            />
          </div>

          <Input
            {...methods.register("email")}
            name="email"
            id="email"
            label="Email"
            type="text"
            placeholder="Enter your email"
            error={methods.formState.errors.email?.message}
            helperText="e.g. username@domain.com"
          />

          <PasswordInput
            {...methods.register("password")}
            id="password"
            name="password"
            label="Password"
            placeholder="Enter your password"
            rules={passwordRules}
          />

          <PasswordInput
            {...methods.register("confirmPassword")}
            name="confirmPassword"
            id="confirmPassword"
            label="Confirm Password"
            placeholder="Confirm your password"
            match={methods.watch("password")}
            helperText="Confirm password must match your password"
          />

          <Button disabled={!allFilled} className="mt-6">
            Create an account
          </Button>

          <p className="text-[12px] mt-4">
            Already have an account?{" "}
            <span className="underline text-blue-05">
              <Link to="/login" className="underline">
                Login
              </Link>
            </span>{" "}
            instead
          </p>
        </form>
      </FormProvider>
    </>
  );
};

export default RegisterPage;
