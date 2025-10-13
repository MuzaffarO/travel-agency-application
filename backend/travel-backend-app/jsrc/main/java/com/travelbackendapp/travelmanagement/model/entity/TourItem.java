package com.travelbackendapp.travelmanagement.model.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;
import java.util.List;
import java.util.Map;

@DynamoDbBean
public class TourItem {

    private String tourId;
    private String name;
    private String destination;
    private String startDate;
    private List<String> startDates;

    private List<String> durations;
    private List<String> mealPlans;

    private Double priceFrom;
    private Map<String, Double> priceByDuration;

    private Map<String, Double> mealSupplementsPerDay;

    private Double rating;
    private Integer reviews;

    private String freeCancellation;
    private Integer freeCancellationDaysBefore;

    private String tourType;

    private Integer maxAdults;
    private Integer maxChildren;
    private Integer availablePackages;

    // --- details content ---
    private List<String> imageUrls;
    private String summary;
    private String accommodation;
    private String hotelName;
    private String hotelDescription;
    private Map<String, String> customDetails;
    private String agentEmail;

    public TourItem() {}

    @DynamoDbPartitionKey @DynamoDbAttribute("tourId")
    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    @DynamoDbAttribute("name")
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    @DynamoDbAttribute("destination")
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    @DynamoDbAttribute("startDate")
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    @DynamoDbAttribute("startDates")
    public List<String> getStartDates() { return startDates; }
    public void setStartDates(List<String> startDates) { this.startDates = startDates; }

    @DynamoDbAttribute("durations")
    public List<String> getDurations() { return durations; }
    public void setDurations(List<String> durations) { this.durations = durations; }

    @DynamoDbAttribute("mealPlans")
    public List<String> getMealPlans() { return mealPlans; }
    public void setMealPlans(List<String> mealPlans) { this.mealPlans = mealPlans; }

    @DynamoDbAttribute("priceFrom")
    public Double getPriceFrom() { return priceFrom; }
    public void setPriceFrom(Double priceFrom) { this.priceFrom = priceFrom; }

    @DynamoDbAttribute("priceByDuration")
    public Map<String, Double> getPriceByDuration() { return priceByDuration; }
    public void setPriceByDuration(Map<String, Double> priceByDuration) { this.priceByDuration = priceByDuration; }

    @DynamoDbAttribute("mealSupplementsPerDay")
    public Map<String, Double> getMealSupplementsPerDay() { return mealSupplementsPerDay; }
    public void setMealSupplementsPerDay(Map<String, Double> mealSupplementsPerDay) { this.mealSupplementsPerDay = mealSupplementsPerDay; }

    @DynamoDbAttribute("rating")
    public Double getRating() { return rating; }
    public void setRating(Double rating) { this.rating = rating; }

    @DynamoDbAttribute("reviews")
    public Integer getReviews() { return reviews; }
    public void setReviews(Integer reviews) { this.reviews = reviews; }

    @DynamoDbAttribute("freeCancellation")
    public String getFreeCancellation() { return freeCancellation; }
    public void setFreeCancellation(String freeCancellation) { this.freeCancellation = freeCancellation; }

    @DynamoDbAttribute("freeCancellationDaysBefore")
    public Integer getFreeCancellationDaysBefore() { return freeCancellationDaysBefore; }
    public void setFreeCancellationDaysBefore(Integer v) { this.freeCancellationDaysBefore = v; }

    @DynamoDbAttribute("tourType")
    public String getTourType() { return tourType; }
    public void setTourType(String tourType) { this.tourType = tourType; }

    @DynamoDbAttribute("maxAdults")
    public Integer getMaxAdults() { return maxAdults; }
    public void setMaxAdults(Integer maxAdults) { this.maxAdults = maxAdults; }

    @DynamoDbAttribute("maxChildren")
    public Integer getMaxChildren() { return maxChildren; }
    public void setMaxChildren(Integer maxChildren) { this.maxChildren = maxChildren; }

    @DynamoDbAttribute("availablePackages")
    public Integer getAvailablePackages() { return availablePackages; }
    public void setAvailablePackages(Integer availablePackages) { this.availablePackages = availablePackages; }

    // --- details content ---
    @DynamoDbAttribute("imageUrls")
    public List<String> getImageUrls() { return imageUrls; }
    public void setImageUrls(List<String> imageUrls) { this.imageUrls = imageUrls; }

    @DynamoDbAttribute("summary")
    public String getSummary() { return summary; }
    public void setSummary(String summary) { this.summary = summary; }

    @DynamoDbAttribute("accommodation")
    public String getAccommodation() { return accommodation; }
    public void setAccommodation(String accommodation) { this.accommodation = accommodation; }

    @DynamoDbAttribute("hotelName")
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    @DynamoDbAttribute("hotelDescription")
    public String getHotelDescription() { return hotelDescription; }
    public void setHotelDescription(String hotelDescription) { this.hotelDescription = hotelDescription; }

    @DynamoDbAttribute("customDetails")
    public Map<String, String> getCustomDetails() { return customDetails; }
    public void setCustomDetails(Map<String, String> customDetails) { this.customDetails = customDetails; }
    @DynamoDbAttribute("agentEmail")
    public String getAgentEmail() { return agentEmail; }
    public void setAgentEmail(String agentEmail) { this.agentEmail = agentEmail; }
}
