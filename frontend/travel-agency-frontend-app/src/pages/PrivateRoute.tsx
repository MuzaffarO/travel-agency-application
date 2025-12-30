import { Navigate } from "react-router-dom";
import { useSelector } from "react-redux";
import type { JSX } from "react/jsx-runtime";
import type { RootState } from "../store/store";

export function PrivateRoute({ children }: { children: JSX.Element }) {
  const { role } = useSelector((state: RootState) => state.user);
  if (!role) {
    return <Navigate to="/login" replace />;
  }
  return children;
}

export function RoleRoute({
  children,
  allowedRoles,
}: {
  children: JSX.Element;
  allowedRoles: string[];
}) {
  const { role } = useSelector((state: RootState) => state.user);
  if (!role) {
    return <Navigate to="/login" replace />;
  }

  if (!allowedRoles.includes(role)) {
    return <Navigate to="/" replace />;
  }
  return children;
}

export function RoleRedirect({ children }: { children: JSX.Element }) {
  const { role } = useSelector((state: RootState) => state.user);

  if (role === "ADMIN") {
    return <Navigate to="/admin/travel-agents" replace />;
  }

  // TRAVEL_AGENT can access main page to browse all tours
  // They will be redirected to /agent/tours only on initial login via other redirects
  // CUSTOMER and others can access main page
  return children;
}
