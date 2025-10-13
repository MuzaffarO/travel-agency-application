package com.travelbackendapp.travelmanagement.model.api.response;

public class ReviewResponse {
    public String authorName;
    public String authorImageUrl;
    public String createdAt;    // yyyy-MM-dd
    public int rate;            // 1..5
    public String reviewContent;
}
