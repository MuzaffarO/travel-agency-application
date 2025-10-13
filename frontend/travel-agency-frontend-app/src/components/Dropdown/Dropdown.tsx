import { useState, useEffect, useRef } from "react";
import { ChevronDown } from "lucide-react";

type DropdownProps = {
  label?: string;
  options: string[];
  defaultValue?: string;
  onSelect?: (option: string) => void;
  placeholder?: string;
  className?: string;
  disabled?: boolean;
};

const Dropdown: React.FC<DropdownProps> = ({
  label,
  options,
  defaultValue,
  onSelect,
  placeholder = "Select an option",
  className = "",
  disabled = false,
}) => {
  const [isOpen, setIsOpen] = useState<boolean>(false);
  const [selectedOption, setSelectedOption] = useState<string>(
    defaultValue || ""
  );
  const dropdownRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (
        dropdownRef.current &&
        !dropdownRef.current.contains(event.target as Node)
      ) {
        setIsOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const handleSelect = (option: string): void => {
    setSelectedOption(option);
    setIsOpen(false);
    if (onSelect) {
      onSelect(option);
    }
  };

  return (
    <div className={`flex items-center gap-3 ${className}`}>
      {label && (
        <span className="text-gray-700 font-medium text-sm whitespace-nowrap">
          {label}
        </span>
      )}
      <div className="relative" ref={dropdownRef}>
        <button
          onClick={() => !disabled && setIsOpen(!isOpen)}
          disabled={disabled}
          className={`flex items-center cursor-pointer justify-between gap-4 px-2 py-1.5 bg-white border-2 rounded-lg button-text transition-colors duration-200 min-w-[160px] ${
            disabled
              ? "border-gray-300 text-gray-400 cursor-not-allowed"
              : "border-blue-05 text-blue-05 hover:bg-blue-02"
          }`}>
          <span className="truncate">{selectedOption || placeholder}</span>
          <ChevronDown
            size={24}
            className={`transition-transform duration-200 flex-shrink-0 ${
              isOpen ? "rotate-180" : ""
            } ${disabled ? "text-gray-400" : ""}`}
          />
        </button>
        {isOpen && !disabled && (
          <div className="absolute top-full mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg z-10 max-h-60 overflow-y-auto">
            {options.map((option: string, index: number) => (
              <button
                key={`${option}-${index}`}
                onClick={() => handleSelect(option)}
                className={`w-full cursor-pointer button-text text-left px-4 py-2 text-sm hover:bg-blue-02 first:rounded-t-lg last:rounded-b-lg transition-colors duration-150 ${
                  selectedOption === option ? "text-blue-05" : "text-blue-09"
                }`}>
                {option}
              </button>
            ))}
          </div>
        )}
      </div>
    </div>
  );
};

export default Dropdown;
