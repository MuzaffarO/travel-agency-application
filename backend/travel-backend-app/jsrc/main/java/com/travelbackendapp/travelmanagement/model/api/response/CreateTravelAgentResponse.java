package com.travelbackendapp.travelmanagement.model.api.response;

public class CreateTravelAgentResponse {
    public String email;
    public String message;

    public CreateTravelAgentResponse() {}
    public CreateTravelAgentResponse(String email, String message) {
        this.email = email;
        this.message = message;
    }
}

