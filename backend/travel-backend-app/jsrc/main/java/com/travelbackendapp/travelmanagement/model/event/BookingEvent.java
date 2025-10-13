package com.travelbackendapp.travelmanagement.model.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.LocalDateTime;

public class BookingEvent {
    
    @JsonProperty("eventType")
    private String eventType; // CONFIRM, CANCEL, FINISH
    
    @JsonProperty("bookingId")
    private String bookingId;
    
    @JsonProperty("userId")
    private String userId;
    
    @JsonProperty("tourId")
    private String tourId;
    
    @JsonProperty("agentEmail")
    private String agentEmail;
    
    @JsonProperty("eventTimestamp")
    private LocalDateTime eventTimestamp;
    
    @JsonProperty("bookingData")
    private BookingData bookingData;
    
    @JsonProperty("agentData")
    private AgentData agentData;
    
    @JsonProperty("feedbackData")
    private FeedbackData feedbackData;
    
    public BookingEvent() {}
    
    public BookingEvent(String eventType, String bookingId, String userId, String tourId, 
                       String agentEmail, LocalDateTime eventTimestamp) {
        this.eventType = eventType;
        this.bookingId = bookingId;
        this.userId = userId;
        this.tourId = tourId;
        this.agentEmail = agentEmail;
        this.eventTimestamp = eventTimestamp;
    }
    
    // Getters and Setters
    public String getEventType() {
        return eventType;
    }
    
    public void setEventType(String eventType) {
        this.eventType = eventType;
    }
    
    public String getBookingId() {
        return bookingId;
    }
    
    public void setBookingId(String bookingId) {
        this.bookingId = bookingId;
    }
    
    public String getUserId() {
        return userId;
    }
    
    public void setUserId(String userId) {
        this.userId = userId;
    }
    
    public String getTourId() {
        return tourId;
    }
    
    public void setTourId(String tourId) {
        this.tourId = tourId;
    }
    
    public String getAgentEmail() {
        return agentEmail;
    }
    
    public void setAgentEmail(String agentEmail) {
        this.agentEmail = agentEmail;
    }
    
    public LocalDateTime getEventTimestamp() {
        return eventTimestamp;
    }
    
    public void setEventTimestamp(LocalDateTime eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    public BookingData getBookingData() {
        return bookingData;
    }
    
    public void setBookingData(BookingData bookingData) {
        this.bookingData = bookingData;
    }
    
    public AgentData getAgentData() {
        return agentData;
    }
    
    public void setAgentData(AgentData agentData) {
        this.agentData = agentData;
    }
    
    public FeedbackData getFeedbackData() {
        return feedbackData;
    }
    
    public void setFeedbackData(FeedbackData feedbackData) {
        this.feedbackData = feedbackData;
    }
    
    // Nested classes for structured data
    public static class BookingData {
        @JsonProperty("status")
        private String status;
        
        @JsonProperty("bookingDate")
        private LocalDateTime bookingDate;
        
        @JsonProperty("travelDate")
        private LocalDateTime travelDate;
        
        @JsonProperty("numberOfGuests")
        private Integer numberOfGuests;
        
        @JsonProperty("totalPrice")
        private Double totalPrice;
        
        @JsonProperty("cancellationReason")
        private String cancellationReason;
        
        public BookingData() {}
        
        // Getters and Setters
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
        
        public LocalDateTime getBookingDate() { return bookingDate; }
        public void setBookingDate(LocalDateTime bookingDate) { this.bookingDate = bookingDate; }
        
        public LocalDateTime getTravelDate() { return travelDate; }
        public void setTravelDate(LocalDateTime travelDate) { this.travelDate = travelDate; }
        
        public Integer getNumberOfGuests() { return numberOfGuests; }
        public void setNumberOfGuests(Integer numberOfGuests) { this.numberOfGuests = numberOfGuests; }
        
        public Double getTotalPrice() { return totalPrice; }
        public void setTotalPrice(Double totalPrice) { this.totalPrice = totalPrice; }
        
        public String getCancellationReason() { return cancellationReason; }
        public void setCancellationReason(String cancellationReason) { this.cancellationReason = cancellationReason; }
    }
    
    public static class AgentData {
        @JsonProperty("agentEmail")
        private String agentEmail;
        
        @JsonProperty("agentName")
        private String agentName;
        
        @JsonProperty("agentRole")
        private String agentRole;
        
        public AgentData() {}
        
        // Getters and Setters
        public String getAgentEmail() { return agentEmail; }
        public void setAgentEmail(String agentEmail) { this.agentEmail = agentEmail; }
        
        public String getAgentName() { return agentName; }
        public void setAgentName(String agentName) { this.agentName = agentName; }
        
        public String getAgentRole() { return agentRole; }
        public void setAgentRole(String agentRole) { this.agentRole = agentRole; }
    }
    
    public static class FeedbackData {
        @JsonProperty("rating")
        private Integer rating;
        
        @JsonProperty("review")
        private String review;
        
        @JsonProperty("feedbackDate")
        private LocalDateTime feedbackDate;
        
        public FeedbackData() {}
        
        // Getters and Setters
        public Integer getRating() { return rating; }
        public void setRating(Integer rating) { this.rating = rating; }
        
        public String getReview() { return review; }
        public void setReview(String review) { this.review = review; }
        
        public LocalDateTime getFeedbackDate() { return feedbackDate; }
        public void setFeedbackDate(LocalDateTime feedbackDate) { this.feedbackDate = feedbackDate; }
    }
}

