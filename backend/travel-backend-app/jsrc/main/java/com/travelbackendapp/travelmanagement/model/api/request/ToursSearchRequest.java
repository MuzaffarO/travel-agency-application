// ToursSearchRequest.java
package com.travelbackendapp.travelmanagement.model.api.request;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.domain.DurationBucket;
import com.travelbackendapp.travelmanagement.domain.GuestQuantity;
import com.travelbackendapp.travelmanagement.domain.SortBy;
import com.travelbackendapp.travelmanagement.exceptions.BadRequestException;

import java.util.*;
import java.util.stream.Collectors;

import static com.travelbackendapp.travelmanagement.util.RequestUtils.*;

public class ToursSearchRequest {
    public final int page;
    public final int pageSize;
    public final String destination;
    public final String startDate; // normalized ISO or null
    public final String endDate;   // normalized ISO or null
    public final String duration;  // still a single value
    public final List<String> mealPlans;
    public final List<String> tourTypes;
    public final SortBy sortBy;
    public final Set<DurationBucket> durationBuckets;
    public final GuestQuantity guests;

    private ToursSearchRequest(
            int page, int pageSize, String destination,
            String startDate, String endDate,
            String duration, List<String> mealPlans, List<String> tourTypes,
            SortBy sortBy, Set<DurationBucket> durationBuckets,
            GuestQuantity guests
    ) {
        this.page = page;
        this.pageSize = pageSize;
        this.destination = destination;
        this.startDate = startDate;
        this.endDate = endDate;
        this.duration = duration;
        this.mealPlans = mealPlans;
        this.tourTypes = tourTypes;
        this.sortBy = sortBy;
        this.durationBuckets = durationBuckets;
        this.guests = guests;
    }

    private static List<String> splitCsv(String raw) {
        if (raw == null || raw.isBlank()) return List.of();
        return Arrays.stream(raw.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    public static ToursSearchRequest fromQuery(Map<String, String> q, ObjectMapper mapper) {
        int page = clampMin(parseIntOrDefault(q.get("page"), 1), 1);
        int pageSize = clampRange(parseIntOrDefault(q.get("pageSize"), 6), 1, 100);

        String destination = valOrNull(q.get("destination"));
        String duration    = valOrNull(q.get("duration"));
        List<String> mealPlans = splitCsv(valOrNull(q.get("mealPlan")));  // BB,HB
        List<String> tourTypes = splitCsv(valOrNull(q.get("tourType")));  // RESORT,CRUISE
        String sort = q.getOrDefault("sortBy", "RATING_DESC");
        SortBy sortBy = SortBy.from(sort);

        // Dates
        String startDate = normalizeIsoDate(valOrNull(q.get("startDate")));
        String endDate   = normalizeIsoDate(valOrNull(q.get("endDate")));
        if (startDate != null && endDate != null && startDate.compareTo(endDate) > 0) {
            throw new BadRequestException("startDate cannot be after endDate");
        }

        // Duration buckets (multi-select)
        Set<DurationBucket> durationBuckets = splitCsv(valOrNull(q.get("durationBucket")))
                .stream()
                .map(DurationBucket::parse)   // returns Optional<DurationBucket>
                .peek(opt -> {
                    if (opt.isEmpty()) {
                        throw new BadRequestException("durationBucket must be one or more of: 1-3, 4-7, 8-12, 13+");
                    }
                })
                .map(Optional::get)
                .collect(Collectors.toCollection(() -> EnumSet.noneOf(DurationBucket.class)));

        // Guests
        GuestQuantity guests = GuestQuantity.from(q, mapper);

        return new ToursSearchRequest(
                page, pageSize, destination, startDate, endDate,
                duration, mealPlans, tourTypes, sortBy, durationBuckets, guests
        );
    }
}
