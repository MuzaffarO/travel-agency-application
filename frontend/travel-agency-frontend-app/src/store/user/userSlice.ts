import { createSlice } from "@reduxjs/toolkit";
import { signIn, signUp, fetchUserInfo } from "./userThunks";

interface UserState {
  isAuth: boolean;
  firstName: string;
  lastName: string;
  email: string;
  token: string;
  role: string;
  userName: string;
  imageUrl?: string;
}

const initialState: UserState = {
  isAuth: false,
  firstName: "",
  lastName: "",
  email: "",
  token: "",
  role: "",
  userName: "",
  imageUrl: undefined,
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
    setUserImageUrl(state, action: { payload: string | null | undefined }) {
      // Add cache-busting parameter if URL doesn't already have query params
      // This ensures the browser fetches the new image instead of using cached version
      let imageUrl = action.payload || undefined;
      if (imageUrl && !imageUrl.includes("?")) {
        imageUrl = `${imageUrl}?v=${Date.now()}`;
      }
      state.imageUrl = imageUrl;
      const storedUser = localStorage.getItem("user");
      if (storedUser) {
        try {
          const user = JSON.parse(storedUser);
          user.imageUrl = imageUrl;
          localStorage.setItem("user", JSON.stringify(user));
        } catch (e) {
          console.warn("Failed to update imageUrl in localStorage:", e);
        }
      }
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
          imageUrl: state.imageUrl,
        })
      );
    });
    builder.addCase(fetchUserInfo.fulfilled, (state, action) => {
      state.firstName = action.payload.firstName;
      state.lastName = action.payload.lastName;
      state.imageUrl = action.payload.imageUrl;
      if (action.payload.role) {
        state.role = action.payload.role;
      }

      const storedUser = localStorage.getItem("user");
      if (storedUser) {
        try {
          const user = JSON.parse(storedUser);
          user.firstName = action.payload.firstName;
          user.lastName = action.payload.lastName;
          user.imageUrl = action.payload.imageUrl;
          if (action.payload.role) {
            user.role = action.payload.role;
          }
          localStorage.setItem("user", JSON.stringify(user));
        } catch (e) {
          console.warn("Failed to update user info in localStorage:", e);
        }
      }
    });
  },
});

export const { logout, restoreUser, setUserImageUrl } = userSlice.actions;
export default userSlice.reducer;
