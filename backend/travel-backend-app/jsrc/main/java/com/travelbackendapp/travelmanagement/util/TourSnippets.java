// util/TourSnippets.java
package com.travelbackendapp.travelmanagement.util;

import com.travelbackendapp.travelmanagement.model.entity.TourItem;

import java.util.List;
import java.util.Locale;

public final class TourSnippets {
    private TourSnippets() {}

    /**
     * Single-line, compact description for LLM context or logs.
     */
    public static String compactLine(TourItem t) {
        String durations = t.getDurations() == null ? "" : String.join("/", t.getDurations());
        String meals     = t.getMealPlans() == null ? "" : String.join("/", t.getMealPlans());
        String price     = formatPrice(t.getPriceFrom());
        String rating    = t.getRating() == null ? "0.0" : String.format(Locale.US, "%.1f", t.getRating());

        return String.format(Locale.ROOT,
                "- id=%s | %s — %s | hotel=%s | type=%s | durations=%s | meals=%s | from=$%s | rating=%s | start=%s",
                nz(t.getTourId()), nz(t.getName()), nz(t.getDestination()), nz(t.getHotelName()),
                nz(t.getTourType()), durations, meals, price, rating, nz(t.getStartDate()));
    }

    /**
     * Lightweight keyword score used to rank relevance of tours against a user query.
     */
    public static int keywordScore(TourItem t, List<String> terms) {
        String hay = (
                nz(t.getName()) + " " + nz(t.getDestination()) + " " + nz(t.getHotelName()) + " " +
                        nz(t.getSummary()) + " " + nz(t.getTourType())
        ).toLowerCase(Locale.ROOT);
        int score = 0;
        for (String term : terms) {
            if (term != null && !term.isBlank() && hay.contains(term.toLowerCase(Locale.ROOT))) {
                score++;
            }
        }
        return score;
    }

    /**
     * Small badge string for UI cards, e.g. "2025-07-12 • $1,999".
     * Uses startDate if present, otherwise the first of startDates; price uses priceFrom.
     */
    public static String shortBadge(TourItem t) {
        String date = t.getStartDate();
        if ((date == null || date.isBlank()) && t.getStartDates() != null && !t.getStartDates().isEmpty()) {
            date = t.getStartDates().get(0);
        }
        if (date == null || date.isBlank()) {
            date = "TBD";
        }
        String price = formatPrice(t.getPriceFrom());
        return date + " • $" + price;
    }

    /**
     * Optional: returns the first image URL or empty string.
     */
    public static String mainImage(TourItem t) {
        List<String> imgs = t.getImageUrls();
        return (imgs != null && !imgs.isEmpty()) ? nz(imgs.get(0)) : "";
    }

    // ---- helpers ----

    private static String formatPrice(Double p) {
        if (p == null) return "—";
        return (Math.floor(p) == p)
                ? String.format(Locale.US, "%,.0f", p)
                : String.format(Locale.US, "%,.2f", p);
    }

    private static String nz(String s) { return s == null ? "" : s; }
}
