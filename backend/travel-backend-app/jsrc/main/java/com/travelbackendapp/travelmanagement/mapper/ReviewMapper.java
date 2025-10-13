package com.travelbackendapp.travelmanagement.mapper;

import com.travelbackendapp.travelmanagement.model.api.response.ReviewResponse;
import com.travelbackendapp.travelmanagement.model.entity.ReviewItem;

public class ReviewMapper {
    public static ReviewResponse toResponse(ReviewItem it) {
        ReviewResponse r = new ReviewResponse();
        r.authorName = it.getAuthorId();
        r.authorImageUrl = it.getAuthorImageUrl();
        r.createdAt = it.getCreatedAt();
        r.rate = it.getRate() == null ? 0 : it.getRate();
        r.reviewContent = it.getReviewContent();
        return r;
    }
}
