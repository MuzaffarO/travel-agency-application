package com.travelbackendapp.travelmanagement.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UpdateProfileResponseDTO {
    
    @JsonProperty("message")
    private String message;
    
    @JsonProperty("success")
    private boolean success;
    
    public UpdateProfileResponseDTO() {}
    
    public UpdateProfileResponseDTO(String message) {
        this.message = message;
        this.success = "Profile updated successfully".equals(message);
    }
    
    public UpdateProfileResponseDTO(String message, boolean success) {
        this.message = message;
        this.success = success;
    }
    
    public String getMessage() {
        return message;
    }
    
    public void setMessage(String message) {
        this.message = message;
    }
    
    public boolean isSuccess() {
        return success;
    }
    
    public void setSuccess(boolean success) {
        this.success = success;
    }
}
