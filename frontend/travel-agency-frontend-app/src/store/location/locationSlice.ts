import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

type LocationState = {
  selectedLocation: string | null;
};

const initialState: LocationState = {
  selectedLocation: null,
};

const locationSlice = createSlice({
  name: "location",
  initialState,
  reducers: {
    setSelectedLocation(state, action: PayloadAction<string | null>) {
      state.selectedLocation = action.payload;
    },
    resetLocation(state) {
      state.selectedLocation = null;
    },
  },
});

export const { setSelectedLocation, resetLocation } = locationSlice.actions;

export default locationSlice.reducer;