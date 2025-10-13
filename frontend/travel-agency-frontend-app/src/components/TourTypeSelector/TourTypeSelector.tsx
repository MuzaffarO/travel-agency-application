import Checkbox from "../../ui/Checkbox";
import {useAppDispatch} from "../../store/hooks/useAppDispatch.ts";
import {useAppSelector} from "../../store/hooks/useAppSelector.ts";
import {toggleTourType} from "../../store/tourTypes/tourTypesSlice.ts";

export interface TourTypeOption {
  label: string;
  value: string;
}

const tourTypeOptions: TourTypeOption[] = [
  { label: "Resorts", value: "RESORT" },
  { label: "Cruises", value: "CRUISE" },
  { label: "Hikes", value: "HIKE" },
];

const TourTypeSelector = () => {
  const dispatch = useAppDispatch();

  const selectedTourTypes = useAppSelector((state) => state.tourTypes.selectedTourTypes);

  const isSelected = (type: TourTypeOption): boolean =>
    selectedTourTypes.some((option) => option.value === type.value);

  const handleCheckboxChange = (type: TourTypeOption) => {
    dispatch(toggleTourType(type));
  };

  return (
    <div className='bg-white p-2 border border-grey-05 rounded-md shadow-card
      flex flex-col gap-4 absolute top-15 z-10 min-w-full
    '>
      {tourTypeOptions.map((option) => (
        <div key={option.value}>
          <Checkbox
            name="tourtype"
            value={option.value}
            isChecked={isSelected(option)}
            caption={option.label}
            onChange={() => handleCheckboxChange(option)}
          />
        </div>
      ))}
    </div>
  )
}
export default TourTypeSelector;