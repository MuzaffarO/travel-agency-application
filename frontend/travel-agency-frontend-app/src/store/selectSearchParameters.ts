import { createSelector } from "@reduxjs/toolkit";
import type {RootState} from "./store.ts";

export const selectSearchParameters = createSelector(
  [
    (state: RootState) => state.location.selectedLocation,
    (state: RootState) => state.dates,
    (state: RootState) => state.guests,
    (state: RootState) => state.meal.selectedMealOptions,
    (state: RootState) => state.tourTypes.selectedTourTypes,
  ],
  (location, dates, guests, mealPlan, tourType) => ({
    destination: location,
    startDate: dates.startDate,
    endDate: dates.endDate,
    durationBucket: dates.durations,
    adults: guests.adults,
    children: guests.children,
    mealPlan: mealPlan.map((m) => m.value),
    tourType: tourType.map((t) => t.value),
  })
);
