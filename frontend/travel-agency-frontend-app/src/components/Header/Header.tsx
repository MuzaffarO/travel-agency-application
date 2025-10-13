import React, { useState, useRef, useEffect } from "react";
import { Luggage, CircleUserRound } from "lucide-react";
import Button from "../Button";
import { Link, useLocation, useNavigate } from "react-router-dom";
import { useSelector } from "react-redux";
import type { RootState } from "../../store/store";
import UserPopup from "../UserPopup";

type UserRole = string;

const Header: React.FC = () => {
  const { userName, role } = useSelector((state: RootState) => state.user);

  const location = useLocation();
  const navigate = useNavigate();
  const [isPopupOpen, setIsPopupOpen] = useState(false);
  const popupRef = useRef<HTMLDivElement>(null);

  //TEST DATA
  // const testRole = "ADMIN";
  //const currentUserRole: UserRole = testRole;

  const currentUserRole: UserRole = userName ? role : "not-logged-in";

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        popupRef.current &&
        !popupRef.current.contains(event.target as Node)
      ) {
        setIsPopupOpen(false);
      }
    };

    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const getNavigationItems = (role: UserRole) => {
    switch (role) {
      case "not-logged-in":
        return [{ label: "All tours", href: "/" }];
      case "CUSTOMER":
        return [
          { label: "All tours", href: "/" },
          { label: "My tours", href: "/my-tours" },
        ];
      case "TRAVEL_AGENT":
        return [
          { label: "All tours", href: "/" },
          { label: "Bookings", href: "/bookings" },
        ];
      case "ADMIN":
        return [{ label: "Reports", href: "/reports" }];
      default:
        return [{ label: "All tours", href: "/" }];
    }
  };

  const getUserDisplayText = (role: UserRole) => {
    switch (role) {
      case "not-logged-in":
        return null;
      case "CUSTOMER":
        return null;
      case "TRAVEL_AGENT":
        return "Travel agent";
      case "ADMIN":
        return "Admin";
      default:
        return null;
    }
  };

  const isActiveTab = (href: string) => {
    return location.pathname === href;
  };

  const handleProfileClick = (e: React.MouseEvent) => {
    e.preventDefault();
    setIsPopupOpen(!isPopupOpen);
  };

  const navigationItems = getNavigationItems(currentUserRole);
  const userDisplayText = getUserDisplayText(currentUserRole);

  return (
    <div className="w-full bg-white relative">
      <div className="px-10 py-3 mx-auto max-w-desktop">
        <div className="grid grid-cols-[1fr_auto_1fr] items-center gap-4">
          <div>
            <a href="/" className="flex items-center gap-2">
              <Luggage className="text-blue-05" size={40} />
              <span className="h2 text-blue-05">Travel Agency</span>
            </a>
          </div>
          <nav className="hidden md:flex items-center gap-2">
            {navigationItems.map((item) => (
              <Link
                key={item.label}
                to={item.href}
                className={`relative px-4 py-2 navigation text-blue-09 hover:text-blue-05 transition-colors ${
                  isActiveTab(item.href)
                    ? "after:content-[''] after:absolute after:-bottom-1.5 after:left-1/2 after:-translate-x-1/2 after:rounded-full after:transition-all after:duration-200 after:w-full after:h-1 after:bg-blue-05"
                    : ""
                }`}
              >
                {item.label}
              </Link>
            ))}
          </nav>
          <div className="flex justify-end relative" ref={popupRef}>
            {currentUserRole === "not-logged-in" ? (
              <Button
                variant="secondary"
                className="px-8"
                onClick={() => {
                  navigate("/login");
                }}
              >
                Sign in
              </Button>
            ) : (
              <div className="flex items-center gap-2.5">
                <a
                  href="/profile"
                  className="flex items-center"
                  onClick={handleProfileClick}
                >
                  <CircleUserRound className="text-blue-09" size={24} />
                </a>
                {userDisplayText && (
                  <span className="navigation text-blue-09">
                    {userDisplayText}
                  </span>
                )}
                {isPopupOpen && <UserPopup />}
              </div>
            )}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Header;
