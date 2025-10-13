package com.travelbackendapp.travelmanagement.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateNameResponseDTO {

    @JsonProperty("message")
    private String message;

    public UpdateNameResponseDTO() {}

    public UpdateNameResponseDTO(String message) { this.message = message; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
