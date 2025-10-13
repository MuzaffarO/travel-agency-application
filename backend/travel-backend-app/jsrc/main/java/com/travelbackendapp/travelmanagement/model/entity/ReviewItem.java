package com.travelbackendapp.travelmanagement.model.entity;

import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

@DynamoDbBean
public class ReviewItem {
    private String bookingId;       // PK: one review per booking

    private String tourId;          // redundant for aggregations
    private String authorId;        // user id (Cognito sub)
    private String authorName;


    private Integer rate;           // 1..5
    private String reviewContent;   // optional comment
    private String authorImageUrl;  // optional avatar

    // ISO dates like "2020-01-30"
    private String createdAt;       // set on create only
    private String updatedAt;       // set on create & update

    @DynamoDbPartitionKey
    @DynamoDbAttribute("bookingId")
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    @DynamoDbAttribute("tourId")
    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    @DynamoDbAttribute("authorId")
    public String getAuthorId() { return authorId; }
    public void setAuthorId(String authorId) { this.authorId = authorId; }

    @DynamoDbAttribute("rate")
    public Integer getRate() { return rate; }
    public void setRate(Integer rate) { this.rate = rate; }

    @DynamoDbAttribute("reviewContent")
    public String getReviewContent() { return reviewContent; }
    public void setReviewContent(String reviewContent) { this.reviewContent = reviewContent; }

    @DynamoDbAttribute("authorImageUrl")
    public String getAuthorImageUrl() { return authorImageUrl; }
    public void setAuthorImageUrl(String authorImageUrl) { this.authorImageUrl = authorImageUrl; }

    @DynamoDbAttribute("createdAt")
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    @DynamoDbAttribute("updatedAt")
    public String getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(String updatedAt) { this.updatedAt = updatedAt; }

    public static String todayIso() {
        return java.time.LocalDate.now(java.time.ZoneOffset.UTC).toString(); // e.g. 2025-10-02
    }
    @DynamoDbAttribute("authorName")
    public String getAuthorName() { return authorName; }
    public void setAuthorName(String authorName) { this.authorName = authorName; }

}
