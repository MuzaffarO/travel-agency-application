package com.travelbackendapp.travelmanagement.mapper;

import com.travelbackendapp.travelmanagement.model.api.response.TourDetailResponse;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;

import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

public final class TourDetailsMapper {
    private TourDetailsMapper() {
    }

    public static TourDetailResponse toDetails(TourItem it) {
        TourDetailResponse r = new TourDetailResponse();
        r.id = it.getTourId();
        r.name = it.getName();
        r.destination = it.getDestination();
        r.rating = it.getRating();
        r.reviews = it.getReviews();

        r.imageUrls = nzList(it.getImageUrls());
        r.summary = it.getSummary();

        r.freeCancellationDaysBefore = deriveCancellationDays(it);

        r.durations = nzList(it.getDurations());
        r.accommodation = it.getAccommodation();
        r.hotelName = it.getHotelName();
        r.hotelDescription = it.getHotelDescription();

        r.mealPlans = expandMealPlans(it.getMealPlans());
        r.customDetails = nzMap(it.getCustomDetails());

        List<String> starts = it.getStartDates();
        if ((starts == null || starts.isEmpty()) && it.getStartDate() != null) {
            starts = List.of(it.getStartDate());
        }
        r.startDates = upcomingSorted(starts == null ? List.of() : starts);


        int maxAdults = nz(it.getMaxAdults());
        int maxChildren = nz(it.getMaxChildren());
        r.guestQuantity = new TourDetailResponse.GuestQuantity(maxAdults, maxChildren, Math.max(0, maxAdults + maxChildren));

        r.price = formatMoneyMap(resolvePriceMap(it));
        r.mealSupplementsPerDay = formatMoneyMap(nzMap(it.getMealSupplementsPerDay()));

        return r;
    }

    // ---- helpers ----
    private static List<String> nzList(List<String> v) {
        return v == null ? List.of() : v;
    }

    private static <K, V> Map<K, V> nzMap(Map<K, V> v) {
        return v == null ? Map.of() : v;
    }

    private static int nz(Integer v) {
        return v == null ? 0 : v;
    }

    private static List<String> upcomingSorted(List<String> dates) {
        if (dates.isEmpty()) return dates;
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        return dates.stream()
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .filter(s -> {
                    try {
                        return !LocalDate.parse(s).isBefore(today);
                    } catch (Exception e) {
                        return false;
                    }
                })
                .sorted()
                .collect(Collectors.toList());
    }

    private static Integer deriveCancellationDays(TourItem it) {
        if (it.getFreeCancellationDaysBefore() != null) return it.getFreeCancellationDaysBefore();

        // derive from freeCancellation (date) to earliest upcoming start date, if both exist
        try {
            String cancelUntil = it.getFreeCancellation();
            List<String> allStarts = new ArrayList<>(nzList(it.getStartDates()));
            if (allStarts.isEmpty() && it.getStartDate() != null) allStarts.add(it.getStartDate());
            if (cancelUntil == null || allStarts.isEmpty()) return null;

            LocalDate cancel = LocalDate.parse(cancelUntil);
            Optional<LocalDate> earliestUpcoming = allStarts.stream().map(LocalDate::parse).min(LocalDate::compareTo);
            if (!earliestUpcoming.isPresent()) return null;

            long days = ChronoUnit.DAYS.between(cancel, earliestUpcoming.get());
            return (int) Math.max(days, 0);
        } catch (Exception ignored) {
            return null;
        }
    }

    private static List<String> expandMealPlans(List<String> codes) {
        if (codes == null) return List.of();
        List<String> out = new ArrayList<>();
        for (String c : codes) {
            if ("BB".equals(c)) out.add("Breakfast (BB)");
            else if ("HB".equals(c)) out.add("Half-board (HB)");
            else if ("FB".equals(c)) out.add("Full-board (FB)");
            else if ("AI".equals(c)) out.add("All inclusive (AI)");
            else out.add(c);
        }
        return out;
    }

    private static Map<String, Double> resolvePriceMap(TourItem it) {
        if (it.getPriceByDuration() != null && !it.getPriceByDuration().isEmpty()) {
            return it.getPriceByDuration();
        }
        List<String> durs = nzList(it.getDurations());
        Double priceFrom = it.getPriceFrom();
        if (!durs.isEmpty() && priceFrom != null && priceFrom > 0) {
            return Map.of(durs.get(0), priceFrom);
        }
        return Map.of();
    }

    private static Map<String, String> formatMoneyMap(Map<String, Double> in) {
        if (in == null || in.isEmpty()) return Map.of();
        Map<String, String> out = new LinkedHashMap<>();
        for (Map.Entry<String, Double> e : in.entrySet()) {
            out.put(e.getKey(), money(e.getValue()));
        }
        return out;
    }

    private static String money(Double v) {
        if (v == null) return "$0";
        if (Math.floor(v) == v) return "$" + String.format(Locale.US, "%.0f", v);
        return "$" + String.format(Locale.US, "%.2f", v);
    }
}
