package com.travelbackendapp.travelmanagement.model.api.response;

import java.util.List;

public class ReviewsPageResponse {
    public List<ReviewResponse> reviews;
    public int page;
    public int pageSize;
    public int totalPages;
    public int totalItems;

    public ReviewsPageResponse(List<ReviewResponse> reviews, int page, int pageSize, int totalPages, int totalItems) {
        this.reviews = reviews;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }
}
