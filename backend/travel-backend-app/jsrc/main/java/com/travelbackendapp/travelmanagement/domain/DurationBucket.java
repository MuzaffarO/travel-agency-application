package com.travelbackendapp.travelmanagement.domain;

import java.util.List;
import java.util.Optional;

public enum DurationBucket {
    B1_3(1, 3),
    B4_7(4, 7),
    B8_12(8, 12),
    B13_PLUS(13, Integer.MAX_VALUE);

    private final int min;
    private final int max;

    DurationBucket(int min, int max) {
        this.min = min;
        this.max = max;
    }

    public static Optional<DurationBucket> parse(String raw) {
        if (raw == null || raw.trim().isEmpty()) return Optional.empty();
        String b = raw.trim();

        // normalize various unicode dashes to ASCII hyphen-minus
        b = b
                .replace('\u2010', '-') // hyphen
                .replace('\u2011', '-') // non-breaking hyphen
                .replace('\u2012', '-') // figure dash
                .replace('\u2013', '-') // en dash
                .replace('\u2014', '-') // em dash
                .replace('\u2015', '-') // horizontal bar
                .replace('\u2212', '-'); // minus sign

        if (b.endsWith("+")) {
            int min = Integer.parseInt(b.substring(0, b.length() - 1).trim());
            return (min == 13) ? Optional.of(B13_PLUS) : Optional.empty();
        }

        String[] parts = b.split("\\s*-\\s*");
        if (parts.length == 2) {
            int a = Integer.parseInt(parts[0]);
            int c = Integer.parseInt(parts[1]);
            if (a == 1 && c == 3) return Optional.of(B1_3);
            if (a == 4 && c == 7) return Optional.of(B4_7);
            if (a == 8 && c == 12) return Optional.of(B8_12);
            return Optional.empty();
        }
        return Optional.empty();
    }


    public boolean matches(List<String> durations) {
        if (durations == null || durations.isEmpty()) return false;
        for (String d : durations) {
            int n = extractLeadingInt(d);
            if (n >= min && n <= max) return true;
        }
        return false;
    }

    private static int extractLeadingInt(String s) {
        if (s == null) return -1;
        int i = 0, n = s.length();
        while (i < n && !Character.isDigit(s.charAt(i))) i++;
        int j = i;
        while (j < n && j < s.length() && Character.isDigit(s.charAt(j))) j++;
        if (i < j) return Integer.parseInt(s.substring(i, j));
        return -1;
    }
}
