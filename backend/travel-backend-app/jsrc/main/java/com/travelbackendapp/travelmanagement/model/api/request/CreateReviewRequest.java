package com.travelbackendapp.travelmanagement.model.api.request;

import javax.validation.constraints.*;

public class CreateReviewRequest {
    @NotBlank(message = "bookingId is required")
    public String bookingId;

    @NotNull(message = "rate is required")
    @Min(value = 1, message = "rate must be between 1 and 5")
    @Max(value = 5, message = "rate must be between 1 and 5")
    public Integer rate;

    @Size(max = 500, message = "comment must be at most 500 characters")
    public String comment;

}
