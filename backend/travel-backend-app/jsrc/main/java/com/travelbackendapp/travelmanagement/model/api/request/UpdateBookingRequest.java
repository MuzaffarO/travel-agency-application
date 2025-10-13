package com.travelbackendapp.travelmanagement.model.api.request;

import java.util.List;

public class UpdateBookingRequest {
    public String date;           // ISO yyyy-MM-dd (optional)
    public String duration;       // e.g. "7 days" (optional)
    public String mealPlan;       // "BB"/"HB"/"FB"/"AI" or label with (BB) (optional)

    public Guests guests;         // optional
    public List<Person> personalDetails; // optional

    public static class Guests {
        public Integer adult;
        public Integer children;
    }
    public static class Person {
        public String firstName;
        public String lastName;
    }
}
