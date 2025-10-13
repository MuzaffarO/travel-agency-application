package com.travelbackendapp.travelmanagement.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignUpResponseDTO {
    
    @JsonProperty("message")
    private String message;
    
    public SignUpResponseDTO() {}
    
    public SignUpResponseDTO(String message) {
        this.message = message;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
}
