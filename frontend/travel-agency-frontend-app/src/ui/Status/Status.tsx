import type StatusProps from "../../models/StatusProps.ts";
import Icon from "../Icon";

const Status: React.FC<StatusProps> = ({ state, label, classNames }) => {
  const stateConfig = {
    completed: {
      icon: "icon-check",
      iconColor: "text-blue-09",
      styles: "border-blue-05",
      bgColor: "bg-white",
    },
    future: {
      icon: null,
      iconColor: null,
      styles: "border-grey-06 text-grey-06",
      bgColor: "bg-blue-01",
    },
    cancelled: {
      icon: "icon-close",
      iconColor: "text-red-04",
      styles: "border-red-04",
      bgColor: "bg-white",
    },
  };

  const config = stateConfig[state] || {
    icon: null,
    iconColor: null,
    styles: "",
    bg: "",
  };

  return (
    <div className={`relative inline-block ${classNames}`}>
      <div
        className={`
          h-8 w-[154px] border rounded-sm relative z-1 border-r-0 flex items-center justify-center gap-2
          ${config.bgColor} ${config.styles}
        `}
      >
        {config.icon && (
          <Icon
            name={config.icon}
            width={28}
            height={28}
            className={config.iconColor}
          />
        )}
        <p className="body-bold">{label}</p>
      </div>
      <span
        className={`
          w-6 h-6 border rounded-sm inline-block absolute z-0 rotate-45 top-1 left-[139px]
          ${config.bgColor} ${config.styles}
        `}
      ></span>
    </div>
  );
};

export default Status;
