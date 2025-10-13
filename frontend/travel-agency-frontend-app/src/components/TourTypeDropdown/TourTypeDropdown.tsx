import DropdownMain from "../DropdownMain";
import TourTypeSelector from "../TourTypeSelector/TourTypeSelector.tsx";
import {useAppSelector} from "../../store/hooks/useAppSelector.ts";

type MealDropdownProps = {
  classNames?: string;
}

const TourTypeDropdown = ({classNames}: MealDropdownProps) => {
  const selectedTourTypes = useAppSelector((state) => state.tourTypes.selectedTourTypes);

  const label = selectedTourTypes.length
    ? selectedTourTypes.map((type) => type.label).join(", ")
    : "Tour type";

  return (
    <DropdownMain
      iconName='icon-tour-type'
      label={label}
      classNames={classNames}
      selector={<TourTypeSelector/>}
    />
  )
}

export default TourTypeDropdown;