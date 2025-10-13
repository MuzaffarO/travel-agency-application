package com.travelbackendapp.travelmanagement.model.api.response;

import java.util.List;

public class DestinationsResponse {
    public List<String> destinations;
    public DestinationsResponse(List<String> destinations) {
        this.destinations = destinations;
    }
}
