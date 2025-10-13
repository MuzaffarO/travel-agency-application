import { zodResolver } from "@hookform/resolvers/zod";
import { useForm, FormProvider } from "react-hook-form";
import { Link, useNavigate } from "react-router-dom";

import Input from "../ui/Input";
import PasswordInput from "../ui/PasswordInput";
import Button from "../components/Button";

import { loginSchema, type LoginFormData } from "../schemas/loginSchema";
import { useAppDispatch } from "../store/hooks/useAppDispatch";
import { signIn } from "../store/user/userThunks";

const LoginPage = () => {
  const methods = useForm<LoginFormData>({
    resolver: zodResolver(loginSchema),
    defaultValues: { email: "", password: "" },
    mode: "onSubmit",
  });

  const dispatch = useAppDispatch();
  const navigate = useNavigate();

  const { watch, setError } = methods;
  const values = watch();
  const allFilled = Object.values(values).every(
    (value) => value && value.trim() !== ""
  );

  const onSubmit = async (data: LoginFormData) => {
    const resultAction = await dispatch(signIn(data));

    if (signIn.fulfilled.match(resultAction)) {
      const user = resultAction.payload as { role: string };

      console.log("Login success:", user);

      if (user.role === "TRAVEL_AGENT") {
        navigate("/");
      } else {
        navigate("/my-tours");
      }
    } else {
      const errorMessage = resultAction.payload as string;

      console.error("Login failed:", errorMessage);

      const serverErrorMessage =
        errorMessage === "Wrong password or email"
          ? "Incorrect email or password. Try again or create an account."
          : errorMessage;

      setError("email", { type: "server", message: serverErrorMessage });
      setError("password", { type: "server", message: serverErrorMessage });
    }
  };

  return (
    <>
      <p className="text-[14px]">WELCOME BACK</p>
      <h1 className="text-6 font-bold">Sign in to your account</h1>

      <FormProvider {...methods}>
        <form
          onSubmit={methods.handleSubmit(onSubmit)}
          className="flex flex-col gap-4 mt-10"
        >
          <Input
            {...methods.register("email")}
            id="email"
            name="email"
            label="Email"
            type="text"
            placeholder="Enter your email"
            error={methods.formState.errors.email?.message}
            helperText="e.g. username@domain.com"
          />
          <div>
            <PasswordInput
              {...methods.register("password")}
              id="password"
              name="password"
              label="Password"
              placeholder="Enter your password"
              error={methods.formState.errors.password?.message}
            />
            <Link to="/forgot-password">
              <span className="text-blue-05 underline text-3 cursor-pointer">
                Forgot password?
              </span>
            </Link>
          </div>

          <Button disabled={!allFilled} className="mt-6">
            Sign in
          </Button>
          <p className="text-[12px] mt-4">
            Don’t have an account?
            <Link to="/register" className="underline text-blue-05 ml-1">
              Create an account
            </Link>
          </p>
        </form>
      </FormProvider>
    </>
  );
};

export default LoginPage;
