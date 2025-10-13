import { configureStore } from "@reduxjs/toolkit";
import userReducer from "./user/userSlice";
import modalReducer from "./modal/modalSlice";

import guestsReducer from "./guests/guestsSlice";
import mealReducer from "./meal/mealSlice";
import datesReducer from "./dates/datesSlice";
import tourTypesReducer from "./tourTypes/tourTypesSlice";
import locationReducer from "./location/locationSlice";

export const store = configureStore({
  reducer: {
    user: userReducer,
    modal: modalReducer,
    guests: guestsReducer,
    meal: mealReducer,
    dates: datesReducer,
    tourTypes: tourTypesReducer,
    location: locationReducer,
  },
});

export type RootState = ReturnType<typeof store.getState>;
export type AppDispatch = typeof store.dispatch;
