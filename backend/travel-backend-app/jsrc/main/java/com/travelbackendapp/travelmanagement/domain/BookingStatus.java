package com.travelbackendapp.travelmanagement.domain;

public enum BookingStatus {
    BOOKED,
    CONFIRMED,
    STARTED,
    CANCELLED,
    FINISHED;

    /** Direct parsing without legacy aliases. */
    public static BookingStatus fromString(String s) {
        if (s == null || s.isBlank()) return BOOKED;
        return BookingStatus.valueOf(s.trim().toUpperCase());
    }

    /** Business rules for allowed transitions. */
    public boolean canTransitionTo(BookingStatus next) {
        if (this.isTerminal()) return false;   // FINISHED & CANCELLED are terminal
        switch (this) {
            case BOOKED:     return next == CONFIRMED || next == STARTED || next == CANCELLED;
            case CONFIRMED:  return next == STARTED || next == CANCELLED;
            case STARTED:    return next == FINISHED;
            default:         return false;
        }
    }

    public boolean isTerminal() {
        return this == FINISHED || this == CANCELLED;
    }
}
