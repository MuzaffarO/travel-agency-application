package com.travelbackendapp.travelmanagement.util;

import com.travelbackendapp.travelmanagement.exceptions.BadRequestException;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

public final class RequestUtils {
    private RequestUtils() {}

    public static boolean isBlank(String s) {
        return s == null || s.trim().isEmpty();
    }

    public static int parseIntOrDefault(String v, int def) {
        try {
            return (v == null || v.isEmpty()) ? def : Integer.parseInt(v);
        } catch (Exception e) {
            return def;
        }
    }

    public static int clampMin(int v, int min) {
        return Math.max(v, min);
    }

    public static int clampRange(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    public static String valOrNull(String v) {
        if (v == null) return null;
        String t = v.trim();
        return t.isEmpty() ? null : t;
    }

    public static String normalizeIsoDate(String s) {
        if (s == null) return null;
        try {
            return LocalDate.parse(s).toString();
        } catch (DateTimeParseException e) {
            throw new BadRequestException("startDate/endDate must be ISO format YYYY-MM-DD");
        }
    }
}
