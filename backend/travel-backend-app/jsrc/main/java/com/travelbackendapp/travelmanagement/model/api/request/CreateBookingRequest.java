package com.travelbackendapp.travelmanagement.model.api.request;

import javax.validation.constraints.*;
import java.util.List;

public class CreateBookingRequest {
    @NotBlank public String userId;
    @NotBlank public String tourId;
    @NotBlank public String date;           // ISO yyyy-MM-dd (start)
    @NotBlank public String duration;       // "7 days"
    @NotBlank public String mealPlan;       // "BB" etc.
    @NotNull public Guests guests;
    @NotNull @Size(min = 1) public List<Person> personalDetails;

    public static class Guests { public int adult; public int children; }
    public static class Person { @NotBlank public String firstName; @NotBlank public String lastName; }
}


