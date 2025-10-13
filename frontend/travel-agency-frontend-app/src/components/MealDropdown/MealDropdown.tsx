import DropdownMain from "../DropdownMain";
import MealSelector from "../MealSelector";
import {useAppSelector} from "../../store/hooks/useAppSelector.ts";

type MealDropdownProps = {
  classNames?: string;
}

const MealDropdown = ({classNames}: MealDropdownProps) => {
  const selectedMealOptions = useAppSelector(state => state.meal.selectedMealOptions);

  const label = selectedMealOptions.length
    ? selectedMealOptions.map((option) => option.label).join(", ")
    : "Meal plan";

  return (
    <DropdownMain
      iconName='icon-meal'
      label={label}
      classNames={classNames}
      selector={<MealSelector />}
    />
  )
}

export default MealDropdown;