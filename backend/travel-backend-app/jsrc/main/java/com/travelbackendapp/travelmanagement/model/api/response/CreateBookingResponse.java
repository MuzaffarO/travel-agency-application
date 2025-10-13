package com.travelbackendapp.travelmanagement.model.api.response;

public class CreateBookingResponse {
    public String bookingId;
    public String freeCancelation;
    public String details;

    public String totalPrice;  // e.g. "$1960"
    public PriceBreakdown breakdown;

    public CreateBookingResponse() {}
    public CreateBookingResponse(String bookingId,String freeCancelation, String details) {
        this.bookingId = bookingId;
        this.freeCancelation = freeCancelation;
        this.details = details;
    }

    public static class PriceBreakdown {
        public String basePerPerson;                 // e.g. "$1400"
        public int days;                             // e.g. 7
        public int guests;                           // e.g. 2
        public String mealSupplementPerDayPerPerson; // e.g. "$20"

        public PriceBreakdown() {}
        public PriceBreakdown(String basePerPerson, int days, int guests, String suppPerDayPerPerson) {
            this.basePerPerson = basePerPerson;
            this.days = days;
            this.guests = guests;
            this.mealSupplementPerDayPerPerson = suppPerDayPerPerson;
        }
    }
}
