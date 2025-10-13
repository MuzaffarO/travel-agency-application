import { createSelector } from "@reduxjs/toolkit";
import { type RootState } from "./store";

export const selectHasActiveFilters = createSelector(
  [
    (state: RootState) => state.location.selectedLocation,
    (state: RootState) => state.dates,
    (state: RootState) => state.guests,
    (state: RootState) => state.meal.selectedMealOptions,
    (state: RootState) => state.tourTypes.selectedTourTypes,
  ],
  (location, dates, guests, mealPlan, tourType) => {
    return Boolean(
      location ||
      dates.startDate ||
      dates.endDate ||
      (dates.durations && dates.durations.length > 0) ||
      guests.adults > 1 ||
      guests.children > 0 ||
      mealPlan.length > 0 ||
      tourType.length > 0
    );
  }
);
