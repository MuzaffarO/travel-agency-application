import Checkbox from "../../ui/Checkbox";
import {useAppDispatch} from "../../store/hooks/useAppDispatch.ts";
import {useAppSelector} from "../../store/hooks/useAppSelector.ts";
import {toggleMealOption} from "../../store/meal/mealSlice.ts";

export interface IMealOption {
  label: string;
  value: string;
}

const mealOptions = [
  { label: "Breakfast (BB)", value: "BB" },
  { label: "Half-board (HB)", value: "HB" },
  { label: "Full-board (FB)", value: "FB" },
  { label: "All inclusive (AI)", value: "AI" },
]

const MealSelector = () => {
  const dispatch = useAppDispatch();

  const selectedMealOptions = useAppSelector(state => state.meal.selectedMealOptions);

  const isSelected = (option: IMealOption): boolean =>
    selectedMealOptions.some((selectedOption) => selectedOption.value === option.value);

  const handleToggleOption = (option: IMealOption) => {
    dispatch(toggleMealOption(option));
  };

  return (
    <div className='bg-white max-w-[199px] p-2 border border-grey-05 rounded-md shadow-card
      flex flex-col gap-4 absolute top-15 z-10 min-w-full
    '>
      {mealOptions.map((option) => (
        <div key={option.value}>
          <Checkbox
            name='meal'
            value={option.value}
            caption={option.label}
            onChange={() => handleToggleOption(option)}
            isChecked={isSelected(option)}
          />
        </div>
      ))}
    </div>
  )
}

export default MealSelector;