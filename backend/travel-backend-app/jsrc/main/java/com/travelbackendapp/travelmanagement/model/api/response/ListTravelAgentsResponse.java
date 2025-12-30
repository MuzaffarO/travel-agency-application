package com.travelbackendapp.travelmanagement.model.api.response;

import java.util.List;

public class ListTravelAgentsResponse {
    public List<TravelAgentDTO> agents;

    public ListTravelAgentsResponse() {}
    public ListTravelAgentsResponse(List<TravelAgentDTO> agents) {
        this.agents = agents;
    }

    public static class TravelAgentDTO {
        public String email;
        public String firstName;
        public String lastName;
        public String role;
        public String createdAt;
        public String createdBy;
        public String phone;
        public String messenger;

        public TravelAgentDTO() {}
    }
}

