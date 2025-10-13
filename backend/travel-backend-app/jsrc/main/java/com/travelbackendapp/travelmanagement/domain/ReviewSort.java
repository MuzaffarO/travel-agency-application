package com.travelbackendapp.travelmanagement.domain;

import com.travelbackendapp.travelmanagement.model.entity.ReviewItem;

import java.util.Comparator;

public enum ReviewSort {
    RATING_DESC, RATING_ASC, NEWEST, OLDEST;

    public static ReviewSort from(String raw) {
        if (raw == null) return RATING_DESC;
        try { return ReviewSort.valueOf(raw.trim()); }
        catch (IllegalArgumentException e) { return RATING_DESC; }
    }

    public Comparator<ReviewItem> comparator() {
        switch (this) {
            case RATING_ASC:  return Comparator.comparing(ReviewItem::getRate, Comparator.nullsLast(Integer::compareTo));
            case NEWEST:
                return Comparator.comparing(
                        ReviewItem::getCreatedAt,
                        Comparator.nullsLast(String::compareTo)
                ).reversed();
            case OLDEST:      return Comparator.comparing(ReviewItem::getCreatedAt);
            case RATING_DESC:
            default:          return Comparator.comparing(ReviewItem::getRate, Comparator.nullsLast(Integer::compareTo)).reversed();
        }
    }
}
