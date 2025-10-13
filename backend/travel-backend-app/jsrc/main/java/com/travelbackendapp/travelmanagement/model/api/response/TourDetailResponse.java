package com.travelbackendapp.travelmanagement.model.api.response;

import java.util.List;
import java.util.Map;

public class TourDetailResponse {
    public String id;
    public String name;
    public String destination;
    public Double rating;
    public Integer reviews;

    public List<String> imageUrls;
    public String summary;

    public Integer freeCancellationDaysBefore;

    public List<String> durations;
    public String accommodation;

    public String hotelName;              // nullable
    public String hotelDescription;       // nullable

    public List<String> mealPlans;        // e.g. "Breakfast (BB)"
    public Map<String, String> customDetails;

    public List<String> startDates;       // ISO-8601 dates

    public GuestQuantity guestQuantity;

    // Money values as strings with symbol, e.g. "$1400"
    public Map<String, String> price;                 // {"7 days":"$1400"}
    public Map<String, String> mealSupplementsPerDay; // {"BB":"$0","HB":"$25","FB":"$40"}

    public static class GuestQuantity {
        public int adultsMaxValue;
        public int childrenMaxValue;
        public int totalMaxValue;

        public GuestQuantity() {}
        public GuestQuantity(int a, int c, int t) {
            this.adultsMaxValue = a;
            this.childrenMaxValue = c;
            this.totalMaxValue = t;
        }
    }
}
