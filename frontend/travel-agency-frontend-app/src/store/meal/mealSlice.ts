import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type { TourTypeOption } from "../../components/TourTypeSelector/TourTypeSelector.tsx";

interface IMealOption {
  label: string;
  value: string;
}

interface MealState {
  selectedMealOptions: IMealOption[];
}

const initialState: MealState = {
  selectedMealOptions: [],
};

const mealSlice = createSlice({
  name: "meal",
  initialState,
  reducers: {
    toggleMealOption(state, action: PayloadAction<TourTypeOption>) {
      const selectedType = action.payload;

      const exists = state.selectedMealOptions.some(
        (option) => option.value === selectedType.value
      );

      if (exists) {
        state.selectedMealOptions = state.selectedMealOptions.filter(
          (option) => option.value !== selectedType.value
        );
      } else {
        state.selectedMealOptions.push(selectedType);
      }
    },
    resetMealOptions(state) {
      state.selectedMealOptions = [];
    },
    setMealOptions(
      state,
      action: PayloadAction<{ selectedMealOptions: IMealOption[] }>
    ) {
      state.selectedMealOptions = action.payload.selectedMealOptions;
    },
  },
});

export const { toggleMealOption, resetMealOptions, setMealOptions } =
  mealSlice.actions;
export default mealSlice.reducer;
