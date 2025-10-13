package com.travelbackendapp.travelmanagement.model.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.travelbackendapp.travelmanagement.validation.ValidPassword;

import javax.validation.constraints.NotBlank;

public class ChangePasswordRequestDTO {

    @JsonProperty("currentPassword")
    @NotBlank(message = "Current password is required")
    private String currentPassword;

    @JsonProperty("newPassword")
    @NotBlank(message = "New password is required")
    @ValidPassword
    private String newPassword;

    public ChangePasswordRequestDTO() {}

    public ChangePasswordRequestDTO(String currentPassword, String newPassword) {
        this.currentPassword = currentPassword;
        this.newPassword = newPassword;
    }

    public String getCurrentPassword() { return currentPassword; }
    public void setCurrentPassword(String currentPassword) { this.currentPassword = currentPassword; }

    public String getNewPassword() { return newPassword; }
    public void setNewPassword(String newPassword) { this.newPassword = newPassword; }
}
