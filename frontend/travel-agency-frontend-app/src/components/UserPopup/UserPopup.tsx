import { UserRound } from "lucide-react";
import { LogOut } from "lucide-react";

import { useSelector, useDispatch } from "react-redux";
import type { RootState } from "../../store/store";
import { logout } from "../../store/user/userSlice";
import { useNavigate } from "react-router-dom";

const UserPopup = () => {
  const dispatch = useDispatch();
  const navigate = useNavigate();
  const { userName, email } = useSelector((state: RootState) => state.user);

  const handleLogout = () => {
    dispatch(logout());
    navigate("/login");
  };
  return (
    <section className="absolute top-14 right-8 z-10 flex flex-col rounded-[8px] bg-white p-4 text-blue-09 border border-grey-05">
      <div className="text-sm pb-4 border-b border-b-grey-05">
        <h2 className="font-extrabold"> {userName}</h2>
        <p className="text-grey-07">{email}</p>
      </div>
      <div
        onClick={() => navigate("/profile")}
        className="flex gap-2 items-center text-sm font-bold mb-2 mt-4 cursor-pointer">
        <UserRound />
        <p>My Profile</p>
      </div>
      <div
        onClick={handleLogout}
        className="flex gap-2 items-center text-sm font-bold cursor-pointer">
        <LogOut />
        <p>Sign Out</p>
      </div>
    </section>
  );
};

export default UserPopup;
