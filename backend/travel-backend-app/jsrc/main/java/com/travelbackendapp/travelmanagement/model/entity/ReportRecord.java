package com.travelbackendapp.travelmanagement.model.entity;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.time.LocalDateTime;

@DynamoDbBean
public class ReportRecord {
    
    @JsonProperty("reportId")
    private String reportId;
    
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
    private String eventTimestamp; // ISO string format for DynamoDB
    
    @JsonProperty("createdAt")
    private String createdAt;
    
    // Booking Data
    @JsonProperty("bookingStatus")
    private String bookingStatus;
    
    @JsonProperty("bookingDate")
    private String bookingDate;
    
    @JsonProperty("travelDate")
    private String travelDate;
    
    @JsonProperty("numberOfGuests")
    private Integer numberOfGuests;
    
    @JsonProperty("totalPrice")
    private Double totalPrice;
    
    @JsonProperty("cancellationReason")
    private String cancellationReason;
    
    // Agent Data
    @JsonProperty("agentName")
    private String agentName;
    
    @JsonProperty("agentRole")
    private String agentRole;
    
    // Feedback Data
    @JsonProperty("rating")
    private Integer rating;
    
    @JsonProperty("review")
    private String review;
    
    @JsonProperty("feedbackDate")
    private String feedbackDate;
    
    public ReportRecord() {}
    
    public ReportRecord(String reportId, String eventType, String bookingId, String userId, 
                       String tourId, String agentEmail, LocalDateTime eventTimestamp) {
        this.reportId = reportId;
        this.eventType = eventType;
        this.bookingId = bookingId;
        this.userId = userId;
        this.tourId = tourId;
        this.agentEmail = agentEmail;
        this.eventTimestamp = eventTimestamp.toString();
        this.createdAt = LocalDateTime.now().toString();
    }
    
    @DynamoDbPartitionKey
    public String getReportId() {
        return reportId;
    }
    
    public void setReportId(String reportId) {
        this.reportId = reportId;
    }
    
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
    
    public String getEventTimestamp() {
        return eventTimestamp;
    }
    
    public void setEventTimestamp(String eventTimestamp) {
        this.eventTimestamp = eventTimestamp;
    }
    
    public String getCreatedAt() {
        return createdAt;
    }
    
    public void setCreatedAt(String createdAt) {
        this.createdAt = createdAt;
    }
    
    public String getBookingStatus() {
        return bookingStatus;
    }
    
    public void setBookingStatus(String bookingStatus) {
        this.bookingStatus = bookingStatus;
    }
    
    public String getBookingDate() {
        return bookingDate;
    }
    
    public void setBookingDate(String bookingDate) {
        this.bookingDate = bookingDate;
    }
    
    public String getTravelDate() {
        return travelDate;
    }
    
    public void setTravelDate(String travelDate) {
        this.travelDate = travelDate;
    }
    
    public Integer getNumberOfGuests() {
        return numberOfGuests;
    }
    
    public void setNumberOfGuests(Integer numberOfGuests) {
        this.numberOfGuests = numberOfGuests;
    }
    
    public Double getTotalPrice() {
        return totalPrice;
    }
    
    public void setTotalPrice(Double totalPrice) {
        this.totalPrice = totalPrice;
    }
    
    public String getCancellationReason() {
        return cancellationReason;
    }
    
    public void setCancellationReason(String cancellationReason) {
        this.cancellationReason = cancellationReason;
    }
    
    public String getAgentName() {
        return agentName;
    }
    
    public void setAgentName(String agentName) {
        this.agentName = agentName;
    }
    
    public String getAgentRole() {
        return agentRole;
    }
    
    public void setAgentRole(String agentRole) {
        this.agentRole = agentRole;
    }
    
    public Integer getRating() {
        return rating;
    }
    
    public void setRating(Integer rating) {
        this.rating = rating;
    }
    
    public String getReview() {
        return review;
    }
    
    public void setReview(String review) {
        this.review = review;
    }
    
    public String getFeedbackDate() {
        return feedbackDate;
    }
    
    public void setFeedbackDate(String feedbackDate) {
        this.feedbackDate = feedbackDate;
    }
}

