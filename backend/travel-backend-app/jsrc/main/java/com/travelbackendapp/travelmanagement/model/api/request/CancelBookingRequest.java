package com.travelbackendapp.travelmanagement.model.api.request;

public class CancelBookingRequest {
    public String cancellationReason; // CUSTOMERS_EMERGENCY | HOTEL_EMERGENCY | SAFETY_CONCERNS | INSUFFICIENT_BOOKINGS | OTHER
    public String comment;
}
