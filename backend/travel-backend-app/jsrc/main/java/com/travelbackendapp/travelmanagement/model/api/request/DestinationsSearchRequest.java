package com.travelbackendapp.travelmanagement.model.api.request;

import com.travelbackendapp.travelmanagement.exceptions.BadRequestException;

import java.util.Map;

public class DestinationsSearchRequest {
    public final String query;
    public final int limit;

    private DestinationsSearchRequest(String query, int limit) {
        this.query = query;
        this.limit = limit;
    }

    public static DestinationsSearchRequest fromQuery(Map<String, String> q) {
        String raw = q == null ? null : q.get("destination");
        if (raw == null || raw.trim().length() < 3) {
            throw new BadRequestException("'destination' query must be at least 3 characters");
        }
        String query = raw.trim();

        int limit = parseIntOrDefault(q != null ? q.get("limit") : null, 10);
        limit = clampRange(limit, 1, 50);

        return new DestinationsSearchRequest(query, limit);
    }

    // ---- tiny helpers (scoped to this DTO) ----
    private static int parseIntOrDefault(String v, int def) {
        if (v == null || v.isEmpty()) return def;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return def; }
    }

    private static int clampRange(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }
}
