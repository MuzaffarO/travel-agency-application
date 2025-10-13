import { Star } from "lucide-react";
import defaultTourImage from "../assets/default-tour.png";

const Testimony = () => {
  return (
    <div className="rounded-xl min-h-[324px] max-w-[316px] bg-white p-6 shadow-card">
      <div className="pb-6 flex w-full items-center justify-between gap-3.5">
        <div className="w-16 h-16 rounded-full overflow-hidden">
          <img
            src={defaultTourImage}
            alt="Profile picture"
            className="w-full h-full object-cover"
          />
        </div>
        <div className="flex-1 flex justify-between">
          <div>
            <p className="body-bold text-blue-09 pb-1">David</p>
            <p className="caption text-blue-09">Aug 6, 2024</p>
          </div>
          <div className="flex gap-1 text-blue-09">
            <Star size={16} fill="#0b3857" />
            <Star size={16} fill="#0b3857" />
            <Star size={16} fill="#0b3857" />
            <Star size={16} fill="#0b3857" />
            <Star size={16} fill="#0b3857" />
          </div>
        </div>
      </div>
      <p className="body text-blue-09">
        Incredible experience! The huts were cozy, and hiking the Brenta
        Dolomites was beyond breathtaking. Highly recommended for adventure
        seekers!
      </p>
    </div>
  );
};

export default Testimony;
