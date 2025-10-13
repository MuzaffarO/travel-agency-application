package com.travelbackendapp.travelmanagement.domain;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public class GuestQuantity {
    public final int adults;
    public final int children;

    public GuestQuantity(int adults, int children) {
        this.adults = Math.max(0, adults);
        this.children = Math.max(0, children);
    }

    @SuppressWarnings("unchecked")
    public static GuestQuantity from(Map<String, String> q, ObjectMapper mapper) {
        int adults = 1, children = 0;
        String gj = q.get("guestQuantity");
        if (gj != null && !gj.isEmpty()) {
            try {
                Map<String, Object> obj = mapper.readValue(gj, Map.class);
                if (obj.get("adults") != null) adults = toInt(obj.get("adults"), adults);
                if (obj.get("children") != null) children = toInt(obj.get("children"), children);
            } catch (Exception ignore) {}
        }
        adults   = parseIntOrDefault(q.get("adults"), adults);
        children = parseIntOrDefault(q.get("children"), children);
        return new GuestQuantity(adults, children);
    }

    private static int toInt(Object o, int def) {
        if (o == null) return def;
        if (o instanceof Number) return ((Number)o).intValue();
        try { return Integer.parseInt(String.valueOf(o)); } catch (Exception e) { return def; }
    }

    private static int parseIntOrDefault(String v, int def) {
        if (v == null || v.isEmpty()) return def;
        try { return Integer.parseInt(v); } catch (NumberFormatException e) { return def; }
    }
}
