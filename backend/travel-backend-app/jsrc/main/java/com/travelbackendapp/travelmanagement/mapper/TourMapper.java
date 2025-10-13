package com.travelbackendapp.travelmanagement.mapper;

import com.travelbackendapp.travelmanagement.model.api.response.TourResponse;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

public class TourMapper {
    public static TourResponse toResponse(TourItem it) {
        TourResponse r = new TourResponse();
        r.id = it.getTourId();
        r.name = it.getName();
        r.destination = it.getDestination();
        r.startDate = it.getStartDate();
        r.tourType = it.getTourType();
        r.imageUrl = firstImage(it.getImageUrls());

        r.durations = it.getDurations() != null ? it.getDurations() : List.of();

        List<String> mp = new ArrayList<>();
        if (it.getMealPlans() != null) {
            for (String code : it.getMealPlans()) {
                if ("BB".equals(code)) mp.add("Breakfast (BB)");
                else if ("HB".equals(code)) mp.add("Half-board (HB)");
                else if ("FB".equals(code)) mp.add("Full-board (FB)");
                else if ("AI".equals(code)) mp.add("All inclusive (AI)");
                else mp.add(code);
            }
        }
        r.mealPlans = mp;

        r.price = "from $" + (it.getPriceFrom()) + " for 1 person";
        r.rating = it.getRating() == null ? 0 : it.getRating().intValue();
        r.reviews = it.getReviews() == null ? 0 : it.getReviews();

        // Prefer stored freeCancellation date; otherwise compute from days-before and startDate.
        r.freeCancelation = computeFreeCancelation(it);

        return r;
    }

    private static String computeFreeCancelation(TourItem it) {
        // If a concrete date is stored, use it.
        String stored = it.getFreeCancellation();
        if (stored != null && !stored.isBlank()) {
            return stored;
        }

        // startDate - freeCancellationDaysBefore
        Integer daysBefore = it.getFreeCancellationDaysBefore();
        String start = it.getStartDate();
        if (daysBefore == null || start == null || start.isBlank()) {
            return null;
        }
        try {
            LocalDate sd = LocalDate.parse(start);            // expects ISO yyyy-MM-dd
            return sd.minusDays(daysBefore).toString();       // ISO yyyy-MM-dd
        } catch (DateTimeParseException e) {
            return null;
        }
    }

    private static String firstImage(java.util.List<String> urls) {
        return (urls == null || urls.isEmpty()) ? null : urls.get(0);
    }
}
