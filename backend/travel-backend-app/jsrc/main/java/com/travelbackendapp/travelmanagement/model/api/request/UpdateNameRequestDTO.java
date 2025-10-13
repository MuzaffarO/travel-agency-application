package com.travelbackendapp.travelmanagement.model.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

public class UpdateNameRequestDTO {

    // Up to 50 chars, Latin letters, hyphens and apostrophes (per your UI note)
    private static final String NAME_REGEX = "^[A-Za-z'\\-\\s]{1,50}$";

    @JsonProperty("firstName")
    @Size(max = 50, message = "First name must be up to 50 characters")
    @Pattern(regexp = NAME_REGEX, message = "Only Latin letters, hyphens, spaces, and apostrophes are allowed")
    private String firstName;   // nullable -> optional

    @JsonProperty("lastName")
    @Size(max = 50, message = "Last name must be up to 50 characters")
    @Pattern(regexp = NAME_REGEX, message = "Only Latin letters, hyphens, spaces, and apostrophes are allowed")
    private String lastName;    // nullable -> optional

    public UpdateNameRequestDTO() {}

    public UpdateNameRequestDTO(String firstName, String lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String v) { this.firstName = v; }

    public String getLastName() { return lastName; }
    public void setLastName(String v) { this.lastName = v; }
}
