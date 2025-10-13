import { createSlice, type PayloadAction } from "@reduxjs/toolkit";

interface GuestsState {
  adults: number;
  children: number;
}

const initialState: GuestsState = {
  adults: 1,
  children: 0,
};

const guestsSlice = createSlice({
  name: "guests",
  initialState,
  reducers: {
    setAdults(state, action: PayloadAction<number>) {
      state.adults = action.payload;
    },
    setChildren(state, action: PayloadAction<number>) {
      state.children = action.payload;
    },
    incrementAdults(state) {
      state.adults += 1;
    },
    decrementAdults(state) {
      state.adults = Math.max(state.adults - 1, 1);
    },
    incrementChildren(state) {
      state.children += 1;
    },
    decrementChildren(state) {
      state.children = Math.max(state.children - 1, 0);
    },
    setGuests: (state, action: PayloadAction<GuestsState>) => {
      state.adults = action.payload.adults;
      state.children = action.payload.children;
    },
    resetGuests: (state) => {
      state.adults = 1;
      state.children = 0;
    }
  },
});

export const {
  setAdults,
  setChildren,
  resetGuests,
  incrementAdults,
  decrementAdults,
  incrementChildren,
  decrementChildren,
  setGuests,
} = guestsSlice.actions;

export default guestsSlice.reducer;
