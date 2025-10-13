import Icon from "../../ui/Icon";
import {useRef, useState} from "react";
import useClickOutside from "../../hooks/useClickOutside.ts";

type DropdownMainProps = {
  classNames?: string;
  iconName?: string;
  label: string;
  selector?: React.ReactNode;
};

const DropdownMain = ({
  classNames,
  iconName,
  label,
  selector,
}: DropdownMainProps) => {
  const [isOpen, setIsOpen] = useState(false);

  const dropdownRef = useRef<HTMLDivElement>(null);
  useClickOutside({ ref: dropdownRef, onClickOutside: () => setIsOpen(false) })

  const toggleDropdown = (event: React.MouseEvent<HTMLDivElement, MouseEvent>) => {
    event.stopPropagation()
    setIsOpen(value => !value)
  }

  return (
    <div className="relative" ref={dropdownRef}>
      <div
        onClick={toggleDropdown}
        className={`
          relative max-h-14 px-3 py-4 border rounded-lg bg-white
          flex items-center gap-2 justify-between
          hover:shadow-card
          ${isOpen ? "border-blue-05" : "border-grey-05"}
          ${classNames}
        `}
      >
        {iconName && <Icon name={iconName} />}{" "}
        <p className="text-sm w-full truncate text-blue-09">{label}</p>
        <Icon name="icon-chevron" className={isOpen ? "transition rotate-180" : ""} />
      </div>

      {isOpen && selector}
    </div>
  );
};

export default DropdownMain;