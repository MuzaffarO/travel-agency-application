import React, { useState, useEffect, useRef } from "react";
import { ChevronDown, User } from "lucide-react";

type IconDropdownProps = {
  label?: string;
  options: string[];
  defaultValue?: string;
  onSelect?: (option: string) => void;
  placeholder?: string;
  className?: string;
  disabled?: boolean;
  icon?: React.ReactNode;
};

const IconDropdown: React.FC<IconDropdownProps> = ({
  options,
  defaultValue,
  onSelect,
  placeholder = "Select an option",
  className = "",
  disabled = false,
  icon,
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
    <div className={`w-full ${className}`}>
      <div className="relative" ref={dropdownRef}>
        <button
          onClick={() => !disabled && setIsOpen(!isOpen)}
          disabled={disabled}
          className={`flex items-center justify-between cursor-pointer w-full px-3 py-3.5 bg-white border border-grey-05 rounded-lg text-sm font-medium transition-all duration-200 ${
            disabled
              ? " cursor-not-allowed bg-gray-50"
              : "border-gray-300 text-gray-700 "
          }`}>
          <div className="flex items-center gap-3">
            {icon || <User className="w-5 h-5 text-gray-500" />}
            <span className="truncate body text-blue-09">
              {selectedOption || placeholder}
            </span>
          </div>
          <ChevronDown
            className={`w-5 h-5 text-gray-400 transition-transform duration-200 flex-shrink-0 ${
              isOpen ? "rotate-180" : ""
            }`}
          />
        </button>

        {isOpen && !disabled && (
          <div className="absolute top-full mt-1 w-full bg-white border border-gray-200 rounded-lg shadow-lg z-10 max-h-60 overflow-y-auto">
            {options.map((option: string, index: number) => (
              <button
                key={`${option}-${index}`}
                onClick={() => handleSelect(option)}
                className={`w-full cursor-pointer button-text text-left px-4 py-2 text-sm hover:bg-blue-02 first:rounded-t-lg last:rounded-b-lg transition-colors duration-150 flex items-center gap-3 ${
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

export default IconDropdown;
