import { type FC } from "react";

type TourTabsProps = {
  activeTab: string;
  onTabChange: (tab: string) => void;
};

const tabs = [
  "All tours",
  "Booked",
  "Confirmed",
  "Started",
  "Finished",
  "Cancelled",
];

const TourTabs: FC<TourTabsProps> = ({ activeTab, onTabChange }) => {
  return (
    <div className="pb-8">
      <nav className="flex">
        {tabs.map((tab) => (
          <button
            key={tab}
            onClick={() => onTabChange(tab)}
            className={`py-3 px-4 body-bold cursor-pointer transition-colors duration-200 relative ${
              activeTab === tab
                ? "text-blue-09"
                : "text-grey-07 hover:text-blue-05"
            }`}
          >
            {tab}
            <div
              className={`absolute left-0 right-0 rounded-full ${
                activeTab === tab
                  ? "bg-blue-05 h-[5px] -bottom-[1.5px]"
                  : "bg-grey-06 h-[1px] bottom-0"
              }`}
            ></div>
          </button>
        ))}
      </nav>
    </div>
  );
};

export default TourTabs;
