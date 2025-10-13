import Logo from "../../ui/Logo";

import backgroundImage from "../../assets/login-image.png";

const Image = () => {
  return (
    <div
      className="hidden md:flex relative w-full items-start h-full overflow-hidden bg-cover p-10 rounded-4xl bg-center bg-no-repeat  flex-col justify-between "
      style={{ backgroundImage: `url(${backgroundImage})` }}
    >
      <div className=" text-white">
        {" "}
        <Logo color="#FFFFFF" />
      </div>
      <h2 className="font-extrabold text-[64px] text-blue-09 ">
        Let’s plan <br />
        your next trip!
      </h2>
    </div>
  );
};

export default Image;
