import { useState } from "react";
import DropdownMain from "../DropdownMain";

type ReportTypeSelectorProps = {
  onChange: (value: string) => void;
};

const ReportTypeSelector = ({ onChange }: ReportTypeSelectorProps) => {
  const [selected, setSelected] = useState<string>("");

  const options = [
    { label: "Staff performance", value: "STAFF_PERFORMANCE" },
    { label: "Sales", value: "SALES" },
  ];

  const handleSelect = (option: { label: string; value: string }) => {
    setSelected(option.label);
    onChange(option.value);
  };

  const selector = (
    <div className="absolute top-full mt-1 w-full bg-white border border-grey-05 rounded-lg shadow-lg z-10">
      {options.map((option) => (
        <button
          key={option.value}
          onClick={() => handleSelect(option)}
          className={`w-full text-left px-4 py-2 text-sm hover:bg-blue-02 first:rounded-t-lg last:rounded-b-lg transition-colors duration-150 ${
            selected === option.label ? "text-blue-05" : "text-blue-09"
          }`}
        >
          {option.label}
        </button>
      ))}
    </div>
  );

  return (
    <DropdownMain
      label={selected || "Select report type"}
      selector={selector}
    />
  );
};

export default ReportTypeSelector;
