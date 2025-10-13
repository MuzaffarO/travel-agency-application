package com.travelbackendapp.travelmanagement.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class UserInfoResponseDTO {
    @JsonProperty("firstName") private String firstName;
    @JsonProperty("lastName")  private String lastName;
    @JsonProperty("imageUrl")  private String imageUrl; // keep for future (e.g., S3 key); can be null
    @JsonProperty("role")      private String role;

    public UserInfoResponseDTO() {}
    public UserInfoResponseDTO(String firstName, String lastName, String imageUrl, String role) {
        this.firstName = firstName; this.lastName = lastName; this.imageUrl = imageUrl; this.role = role;
    }
    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }
    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String v) { this.imageUrl = v; }
    public String getRole() { return role; }
    public void setRole(String v) { this.role = v; }
}
