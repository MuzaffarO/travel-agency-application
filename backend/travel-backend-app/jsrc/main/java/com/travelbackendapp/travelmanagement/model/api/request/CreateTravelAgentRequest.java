package com.travelbackendapp.travelmanagement.model.api.request;

import javax.validation.constraints.*;

public class CreateTravelAgentRequest {
    @NotBlank
    @Email
    public String email;
    
    @NotBlank
    public String firstName;
    
    @NotBlank
    public String lastName;
    
    @NotBlank
    @Pattern(regexp = "TRAVEL_AGENT|ADMIN", message = "Role must be either TRAVEL_AGENT or ADMIN")
    public String role;
    
    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$", 
             message = "Password must contain at least one uppercase, one lowercase, one digit, and one special character")
    public String password;
    
    public String phone;
    public String messenger;
}

