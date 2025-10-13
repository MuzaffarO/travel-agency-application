package com.travelbackendapp.travelmanagement.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SignInResponseDTO {
    
    @JsonProperty("idToken")
    private String idToken;
    
    @JsonProperty("role")
    private String role;
    
    @JsonProperty("userName")
    private String userName;
    
    @JsonProperty("email")
    private String email;
    
    public SignInResponseDTO() {}
    
    public SignInResponseDTO(String idToken, String role, String userName, String email) {
        this.idToken = idToken;
        this.role = role;
        this.userName = userName;
        this.email = email;
    }
    
    public String getIdToken() {
        return idToken;
    }
    
    public void setIdToken(String idToken) {
        this.idToken = idToken;
    }
    
    public String getRole() {
        return role;
    }
    
    public void setRole(String role) {
        this.role = role;
    }
    
    public String getUserName() {
        return userName;
    }
    
    public void setUserName(String userName) {
        this.userName = userName;
    }
    
    public String getEmail() {
        return email;
    }
    
    public void setEmail(String email) {
        this.email = email;
    }
}
