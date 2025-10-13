package com.travelbackendapp.travelmanagement.model.api.request;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotBlank;

public class UploadAvatarRequestDTO {

    @JsonProperty("imageBase64")
    @NotBlank(message = "imageBase64 is required")
    private String imageBase64;

    public UploadAvatarRequestDTO() {}
    public UploadAvatarRequestDTO(String imageBase64){ this.imageBase64 = imageBase64; }

    public String getImageBase64() { return imageBase64; }
    public void setImageBase64(String imageBase64) { this.imageBase64 = imageBase64; }
}
