package com.travelbackendapp.travelmanagement.domain;

import com.travelbackendapp.travelmanagement.model.entity.TourItem;

import java.util.Comparator;

public enum SortBy {
    RATING_DESC,
    RATING_ASC,
    PRICE_DESC,
    PRICE_ASC;

    public static SortBy from(String s) {
        if (s == null) return RATING_DESC;
        try { return SortBy.valueOf(s); }
        catch (IllegalArgumentException ex) { return RATING_DESC; }
    }

    public Comparator<TourItem> comparator() {
        switch (this) {
            case RATING_ASC:
                return Comparator.comparing(TourItem::getRating, Comparator.nullsLast(Double::compareTo));
            case PRICE_DESC:
                return Comparator.comparing(TourItem::getPriceFrom, Comparator.nullsLast(Double::compareTo)).reversed();
            case PRICE_ASC:
                return Comparator.comparing(TourItem::getPriceFrom, Comparator.nullsLast(Double::compareTo));
            case RATING_DESC:
            default:
                return Comparator.comparing(TourItem::getRating, Comparator.nullsLast(Double::compareTo)).reversed();
        }
    }
}
