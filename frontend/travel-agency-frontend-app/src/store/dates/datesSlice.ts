import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

interface DatesState {
  startDate: string | null;
  endDate: string | null;
  durations: string[];
}

const initialState: DatesState = {
  startDate: null,
  endDate: null,
  durations: [],
};

const datesSlice = createSlice({
  name: "dates",
  initialState,
  reducers: {
    setStartDate(state, action: PayloadAction<string | null>) {
      state.startDate = action.payload;
    },
    setEndDate(state, action: PayloadAction<string | null>) {
      state.endDate = action.payload;
    },
    toggleDuration(state, action: PayloadAction<string>) {
      const duration = action.payload;
      if (state.durations.includes(duration)) {
        state.durations = state.durations.filter((item) => item !== duration);
      } else {
        state.durations.push(duration);
      }
    },
    resetDates(state) {
      state.startDate = null;
      state.endDate = null;
      state.durations = [];
    },
    setDates(
      state,
      action: PayloadAction<{
        startDate: string;
        endDate?: string;
        durations: string[];
      }>
    ) {
      state.startDate = action.payload.startDate;
      state.endDate = action.payload.endDate ?? null;
      state.durations = action.payload.durations;
    },
  },
});

export const {
  setStartDate,
  setEndDate,
  toggleDuration,
  resetDates,
  setDates,
} = datesSlice.actions;

export default datesSlice.reducer;
