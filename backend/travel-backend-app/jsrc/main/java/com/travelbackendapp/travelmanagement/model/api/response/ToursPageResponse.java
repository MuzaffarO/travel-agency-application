package com.travelbackendapp.travelmanagement.model.api.response;

import com.travelbackendapp.travelmanagement.model.api.response.TourResponse;

import java.util.List;

public class ToursPageResponse {
    public List<TourResponse> tours;
    public int page;
    public int pageSize;
    public int totalPages;
    public int totalItems;

    public ToursPageResponse(List<TourResponse> tours, int page, int pageSize, int totalPages, int totalItems) {
        this.tours = tours;
        this.page = page;
        this.pageSize = pageSize;
        this.totalPages = totalPages;
        this.totalItems = totalItems;
    }
}
