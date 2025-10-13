import { createSlice, type PayloadAction } from "@reduxjs/toolkit";
import type {TourTypeOption} from "../../components/TourTypeSelector/TourTypeSelector.tsx";

interface TourTypeState {
  selectedTourTypes: TourTypeOption[];
}

const initialState: TourTypeState = {
  selectedTourTypes: [],
};

const tourTypeSlice = createSlice({
  name: "tourTypes",
  initialState,
  reducers: {
    toggleTourType(state, action: PayloadAction<TourTypeOption>) {
      const selectedType = action.payload;

      const exists = state.selectedTourTypes.some((option) => option.value === selectedType.value);

      if (exists) {
        state.selectedTourTypes = state.selectedTourTypes.filter(
          (option) => option.value !== selectedType.value
        );
      } else {
        state.selectedTourTypes.push(selectedType);
      }
    },
    resetTourTypes(state) {
      state.selectedTourTypes = [];
    },
  },
});

export const { toggleTourType, resetTourTypes } = tourTypeSlice.actions;
export default tourTypeSlice.reducer;