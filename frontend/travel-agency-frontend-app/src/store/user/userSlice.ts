import { createSlice } from "@reduxjs/toolkit";
import { signIn, signUp } from "./userThunks";

interface UserState {
  isAuth: boolean;
  firstName: string;
  lastName: string;
  email: string;
  token: string;
  role: string;
  userName: string;
}

const initialState: UserState = {
  isAuth: false,
  firstName: "",
  lastName: "",
  email: "",
  token: "",
  role: "",
  userName: "",
};

const userSlice = createSlice({
  name: "user",
  initialState,
  reducers: {
    logout() {
      localStorage.removeItem("user");
      return initialState;
    },
    restoreUser(state) {
      const storedUser = localStorage.getItem("user");
      if (storedUser) {
        try {
          return JSON.parse(storedUser) as UserState;
        } catch (e) {
          console.warn("Invalid JSON in localStorage:", e);
          return state;
        }
      }
      return state;
    },
  },
  extraReducers: (builder) => {
    builder.addCase(signUp.fulfilled, (state, action) => {
      state.isAuth = true;
      state.firstName = action.payload.firstName;
      state.lastName = action.payload.lastName;
      state.email = action.payload.email;
      localStorage.setItem(
        "user",
        JSON.stringify({
          isAuth: true,
          firstName: action.payload.firstName,
          lastName: action.payload.lastName,
          email: action.payload.email,
          token: "",
          role: "",
          userName: "",
        })
      );
    });
    builder.addCase(signIn.fulfilled, (state, action) => {
      state.isAuth = true;
      state.email = action.payload.email;
      state.role = action.payload.role;
      state.userName = action.payload.userName;
      state.token = action.payload.idToken;

      localStorage.setItem(
        "user",
        JSON.stringify({
          isAuth: true,
          role: action.payload.role,
          email: action.payload.email,
          token: action.payload.idToken,
          userName: action.payload.userName,
        })
      );
    });
  },
});

export const { logout, restoreUser } = userSlice.actions;
export default userSlice.reducer;
