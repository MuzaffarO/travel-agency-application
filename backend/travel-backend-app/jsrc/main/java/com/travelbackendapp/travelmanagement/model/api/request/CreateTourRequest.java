package com.travelbackendapp.travelmanagement.model.api.request;

import javax.validation.constraints.*;
import java.util.List;
import java.util.Map;

public class CreateTourRequest {
    @NotBlank public String name;
    @NotBlank public String destination;
    @NotNull @Size(min = 1) public List<String> startDates; // ISO yyyy-MM-dd
    @NotNull @Size(min = 1) public List<String> durations; // e.g. ["7 days", "10 days"]
    @NotNull @Size(min = 1) public List<String> mealPlans; // e.g. ["BB", "HB", "FB", "AI"]
    
    @NotNull public Double priceFrom;
    @NotNull public Map<String, Double> priceByDuration; // e.g. {"7 days": 1400.0}
    @NotNull public Map<String, Double> mealSupplementsPerDay; // e.g. {"BB": 0.0, "HB": 25.0}
    
    public String tourType;
    @NotNull @Min(1) public Integer maxAdults;
    @NotNull @Min(0) public Integer maxChildren;
    @NotNull @Min(1) public Integer availablePackages;
    
    public List<String> imageUrls;
    public String summary;
    public String accommodation;
    public String hotelName;
    public String hotelDescription;
    public Map<String, String> customDetails;
    
    public String freeCancellation; // e.g. "Free cancellation"
    public Integer freeCancellationDaysBefore;
}

