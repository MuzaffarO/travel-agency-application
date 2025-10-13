import { Navigate, Route, Routes, useNavigate } from "react-router-dom";
import AuthLayout from "./layouts/AuthLayout";
import MainLayout from "./layouts/MainLayout";
import LoginPage from "./pages/LoginPage";
import MainPage from "./pages/MainPage";
import MyToursPage from "./pages/MyToursPage";
import RegisterPage from "./pages/RegisterPage";
import { store } from "./store/store";
import { restoreUser } from "./store/user/userSlice";
import TourDetails from "./pages/TourDetailsPage";
import ReportPage from "./pages/ReportPage";
import ForgotPasswordPage from "./pages/ForgotPasswordPage";
import globalRouter from "./globalRouter.ts";
import {
  PrivateRoute,
  RoleRedirect,
  RoleRoute,
} from "./pages/PrivateRoute.tsx";
import BookingsPage from "./pages/BookingsPage";
import ProfilePage from "./pages/ProfilePage";
import ProfileLayout from "./layouts/ProfileLayout";

const savedUser = localStorage.getItem("user");
if (savedUser) {
  const parsed = JSON.parse(savedUser);
  store.dispatch(restoreUser(parsed));
}

function App() {
  globalRouter.navigate = useNavigate();

  return (
    <div className="bg-blue-03 font-family-nunito">
      <Routes>
        <Route element={<AuthLayout />}>
          <Route path="/register" element={<RegisterPage />} />
          <Route path="/login" element={<LoginPage />} />
          <Route path="/forgot-password" element={<ForgotPasswordPage />} />
        </Route>
        <Route element={<MainLayout />}>
          <Route
            path="/"
            element={
              <RoleRedirect>
                <MainPage />
              </RoleRedirect>
            }
          />

          <Route
            path="/my-tours"
            element={
              <PrivateRoute>
                <RoleRedirect>
                  <MyToursPage />
                </RoleRedirect>
              </PrivateRoute>
            }
          />

          <Route path="/tours/:id" element={<TourDetails />} />
          <Route
            path="/reports"
            element={
              <RoleRoute allowedRoles={["ADMIN"]}>
                <ReportPage />
              </RoleRoute>
            }
          />
          <Route path="/bookings" element={<BookingsPage />} />
          <Route path="/profile" element={<ProfileLayout />}>
            <Route index element={<ProfilePage />} />
            <Route path="change-password" element={<ProfilePage />} />
          </Route>
        </Route>
        <Route path="*" element={<Navigate to="/" replace />} />
      </Routes>
    </div>
  );
}

export default App;
