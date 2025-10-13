import DropdownMain from "../DropdownMain";
import GuestsSelector from "../GuestsSelector";
import { useAppSelector } from "../../store/hooks/useAppSelector.ts";

type GuestsDropdownProps = {
  classNames?: string;
  adults?: number;
  children?: number;
  onChange?: (guests: { adults: number; children: number }) => void; // додали onChange
};

const getGuestsLabel = (adults: number = 1, children: number = 0): string => {
  if (children > 0) {
    return `${adults} adult${adults > 1 ? "s" : ""}, ${children} child${children > 1 ? "ren" : ""}`;
  }
  return `${adults} adult${adults > 1 ? "s" : ""}`;
};

const GuestsDropdown = ({
  classNames,
  adults,
  children,
}: GuestsDropdownProps) => {
  const guestsFromStore = useAppSelector((state) => state.guests);

  const actualAdults = adults ?? guestsFromStore.adults;
  const actualChildren = children ?? guestsFromStore.children;

  const label = getGuestsLabel(actualAdults, actualChildren);

  return (
    <DropdownMain
      iconName="icon-people"
      label={label}
      classNames={classNames}
      selector={<GuestsSelector />}
    />
  );
};

export default GuestsDropdown;
