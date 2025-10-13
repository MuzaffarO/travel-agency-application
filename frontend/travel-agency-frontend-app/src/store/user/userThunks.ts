import { createAsyncThunk } from "@reduxjs/toolkit";
import axios from "axios";
import { BACK_URL } from "../../constants";

interface SignUpResponse {
  id: string;
  firstName: string;
  lastName: string;
  email: string;
}

interface SignInResponse {
  idToken: string;
  role: string;
  userName: string;
  email: string;
}

export const signUp = createAsyncThunk<
  SignUpResponse,
  { firstName: string; lastName: string; email: string; password: string },
  { rejectValue: string }
>("user/signUp", async (userData, thunkAPI) => {
  try {
    const { data } = await axios.post(`${BACK_URL}/auth/sign-up`, userData, {
      headers: { "Content-Type": "application/json" },
    });
    console.log("SignUp response:", data);
    return data;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      interface ErrorResponse {
        message?: string;
      }
      const errorData = error.response?.data as ErrorResponse | undefined;
      const message = errorData?.message || "Unknown error occurred.";
      return thunkAPI.rejectWithValue(message);
    }
    if (error instanceof Error) {
      return thunkAPI.rejectWithValue(
        error.message || "Unknown error occurred."
      );
    }
    return thunkAPI.rejectWithValue("Unknown error occurred.");
  }
});

export const signIn = createAsyncThunk<
  SignInResponse,
  { email: string; password: string },
  { rejectValue: string }
>("user/signIn", async (credentials, thunkAPI) => {
  try {
    const { data } = await axios.post(`${BACK_URL}/auth/sign-in`, credentials, {
      headers: { "Content-Type": "application/json" },
    });
    console.log("SignIn response:", data);
    return data;
  } catch (error: unknown) {
    if (axios.isAxiosError(error)) {
      interface ErrorResponse {
        message?: string;
      }
      const errorData = error.response?.data as ErrorResponse | undefined;
      const message = errorData?.message || "Login failed";
      return thunkAPI.rejectWithValue(message);
    }
    if (error instanceof Error) {
      return thunkAPI.rejectWithValue(
        error.message || "Unknown error occurred."
      );
    }
    return thunkAPI.rejectWithValue("Unknown error occurred.");
  }
});
