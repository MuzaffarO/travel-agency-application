package com.travelbackendapp.travelmanagement.model.api.response;

public class UpdateTourResponse {
    public String tourId;
    public String message;

    public UpdateTourResponse() {}
    public UpdateTourResponse(String tourId, String message) {
        this.tourId = tourId;
        this.message = message;
    }
}

