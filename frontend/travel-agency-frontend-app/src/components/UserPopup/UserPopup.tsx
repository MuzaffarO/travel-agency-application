import { UserRound, LogOut, User } from "lucide-react";
import { useSelector, useDispatch } from "react-redux";
import type { RootState } from "../../store/store";
import { logout } from "../../store/user/userSlice";
import { useNavigate } from "react-router-dom";
import { useState } from "react";

const UserPopup = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { userName, email, role, firstName, lastName, imageUrl } = useSelector((state: RootState) => state.user);
  const [imageError, setImageError] = useState(false);

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
  };

  const getRoleDisplayName = (role: string) => {
    switch (role) {
      case "TRAVEL_AGENT":
        return "Travel Agent";
      case "ADMIN":
        return "Administrator";
      case "CUSTOMER":
        return "Customer";
      default:
        return "User";
    }
  };

  const getRoleBadgeColor = (role: string) => {
    switch (role) {
      case "TRAVEL_AGENT":
        return "bg-blue-05 text-white";
      case "ADMIN":
        return "bg-purple-600 text-white";
      case "CUSTOMER":
        return "bg-green-600 text-white";
      default:
        return "bg-gray-500 text-white";
    }
  };

  const fullName = firstName && lastName ? `${firstName} ${lastName}` : userName || "User";

  return (
    <section className="absolute top-14 right-8 z-10 flex flex-col rounded-xl bg-white shadow-lg border border-grey-05 min-w-[280px] overflow-hidden">
      {/* Header Section with Avatar and Role */}
      <div className="bg-gradient-to-r from-blue-05 to-blue-07 p-6 pb-8">
        <div className="flex items-center gap-4 mb-4">
          {imageUrl && !imageError ? (
            <img
              src={imageUrl}
              alt="Profile"
              className="w-16 h-16 rounded-full object-cover border-4 border-white shadow-md"
              onError={() => setImageError(true)}
            />
          ) : (
            <div className="w-16 h-16 rounded-full bg-white flex items-center justify-center border-4 border-white shadow-md">
              <User className="w-8 h-8 text-blue-09" />
            </div>
          )}
          <div className="flex-1 min-w-0">
            <h2 className="font-bold text-white text-lg truncate">{fullName}</h2>
            <p className="text-blue-100 text-sm truncate">{email}</p>
          </div>
        </div>
        {role && (
          <div className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${getRoleBadgeColor(role)}`}>
            {getRoleDisplayName(role)}
          </div>
        )}
      </div>

      {/* Action Items */}
      <div className="p-2">
        <div
          onClick={() => {
            navigate("/profile");
          }}
          className="flex gap-3 items-center px-4 py-3 rounded-lg text-blue-09 font-medium cursor-pointer hover:bg-blue-50 transition-colors group">
          <UserRound className="w-5 h-5 group-hover:text-blue-05 transition-colors" />
          <span>My Profile</span>
        </div>
        <div
          onClick={handleLogout}
          className="flex gap-3 items-center px-4 py-3 rounded-lg text-red-600 font-medium cursor-pointer hover:bg-red-50 transition-colors group">
          <LogOut className="w-5 h-5 group-hover:text-red-700 transition-colors" />
          <span>Sign Out</span>
        </div>
      </div>
    </section>
  );
};

export default UserPopup;
