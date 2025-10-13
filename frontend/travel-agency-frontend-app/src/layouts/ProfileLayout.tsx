import { NavLink, Outlet } from "react-router-dom";

export default function ProfileLayout() {
  return (
    <div className="relative min-h-[320px]">
      <aside className="hidden md:block absolute left-0 top-6 w-60 z-20">
        <h2 className="h1 text-blue-09 mb-6">My profile</h2>

        <nav className="flex flex-col gap-3">
          <NavLink
            to="/profile"
            end
            className={({ isActive }) =>
              `pl-3 h3 border-l-2 ${
                isActive
                  ? "border-blue-05 text-blue-09"
                  : "border-transparent text-grey-07"
              }`
            }>
            General information
          </NavLink>

          <NavLink
            to="/profile/change-password"
            end
            className={({ isActive }) =>
              `pl-3 h3 border-l-2 ${
                isActive
                  ? "border-blue-05 text-blue-09"
                  : "border-transparent text-grey-07"
              }`
            }>
            Change password
          </NavLink>
        </nav>
      </aside>

      <div className="flex justify-center">
        <div className="w-full flex justify-center max-w-[760px] pt-6">
          <Outlet />
        </div>
      </div>
    </div>
  );
}
