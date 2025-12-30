package com.travelbackendapp.travelmanagement.model.api.request;

import java.util.List;
import java.util.Map;

public class UpdateTourRequest {
    public String name;
    public String destination;
    public List<String> startDates; // ISO yyyy-MM-dd
    public List<String> durations; // e.g. ["7 days", "10 days"]
    public List<String> mealPlans; // e.g. ["BB", "HB", "FB", "AI"]
    
    public Double priceFrom;
    public Map<String, Double> priceByDuration; // e.g. {"7 days": 1400.0}
    public Map<String, Double> mealSupplementsPerDay; // e.g. {"BB": 0.0, "HB": 25.0}
    
    public String tourType;
    public Integer maxAdults;
    public Integer maxChildren;
    public Integer availablePackages;
    
    public List<String> imageUrls;
    public String summary;
    public String accommodation;
    public String hotelName;
    public String hotelDescription;
    public Map<String, String> customDetails;
    
    public String freeCancellation; // e.g. "Free cancellation"
    public Integer freeCancellationDaysBefore;
}

