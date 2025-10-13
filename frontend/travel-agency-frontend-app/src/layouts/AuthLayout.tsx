import { Outlet } from "react-router-dom";
import Image from "../components/Image";

import { Toaster } from "react-hot-toast";

const AuthLayout = () => {
  return (
    <section className=" grid md:grid-cols-[1fr_1fr] bg-inherit text-blue-09 px-4 py-4 lg:px-10 lg:py-6 gap-8 m-auto min-h-[100vh] max-w-[1440px]">
      <div className="rounded-[32px] bg-white p-8 lg:px-[84px] lg:py-[54px] shadow-[0px_2px_10px_6px_#027EAC33] flex flex-col justify-center">
        <Outlet />
      </div>

      <Image />
      <Toaster position="top-right" />
    </section>
  );
};

export default AuthLayout;
