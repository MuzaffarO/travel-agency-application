package com.travelbackendapp.travelmanagement.model.entity;

import com.travelbackendapp.travelmanagement.domain.BookingStatus;
import software.amazon.awssdk.enhanced.dynamodb.AttributeConverter;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.*;

import java.util.*;

import software.amazon.awssdk.enhanced.dynamodb.AttributeValueType;
import software.amazon.awssdk.enhanced.dynamodb.EnhancedType;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

@DynamoDbBean
public class BookingItem {

    // PK/SK (query my-bookings fast; later GSI can be implemented)
    private String userId;      // PK
    private String bookingId;   // SK: yyyy-MM-dd#<uuid>

    private String tourId;
    private String tourName;
    private String destination;
    private String hotelName;
    private Double tourRating;

    private String startDate;               // ISO yyyy-MM-dd
    private String duration;                // e.g. "7 days"
    private String mealPlan;                // "BB" / "HB" / ...

    private Integer adults;
    private Integer children;

    // Free cancellation policy
    private String freeCancelationUntil;    // ISO yyyy-MM-dd (derived)

    // Server-managed
    private String status;                  // BOOKED | CONFIRMED | STARTED | CANCELLED | FINISHED
    private Long createdAtEpoch;            // ms epoch
    private String agentContact;            // optional, if assigned
    private List<Person> personalDetails;
    private String agentEmail;   // snapshot
    private String agentName;    // snapshot "First Last"
    private Double totalPrice;
    private Long confirmedAtEpoch;   // ms epoch (nullable)
    private String cancellationReason;
    private String cancellationComment;

    private Long   cancelledAtEpoch;
    private String cancelledBy;
    private String customerEmail;
    private String customerPhone;

    @DynamoDbAttribute("cancelledBy")
    public String getCancelledBy() { return cancelledBy; }
    public void setCancelledBy(String v) { this.cancelledBy = v; }

    @DynamoDbAttribute("cancellationReason")
    public String getCancellationReason() { return cancellationReason; }
    public void setCancellationReason(String v) { this.cancellationReason = v; }

    @DynamoDbAttribute("cancellationComment")
    public String getCancellationComment() { return cancellationComment; }
    public void setCancellationComment(String v) { this.cancellationComment = v; }

    @DynamoDbAttribute("cancelledAtEpoch")
    public Long getCancelledAtEpoch() { return cancelledAtEpoch; }
    public void setCancelledAtEpoch(Long v) { this.cancelledAtEpoch = v; }

    @DynamoDbPartitionKey @DynamoDbAttribute("userId")
    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    @DynamoDbSortKey @DynamoDbAttribute("bookingId")
    public String getBookingId() { return bookingId; }
    public void setBookingId(String bookingId) { this.bookingId = bookingId; }

    @DynamoDbAttribute("tourId")
    public String getTourId() { return tourId; }
    public void setTourId(String tourId) { this.tourId = tourId; }

    @DynamoDbAttribute("tourName")
    public String getTourName() { return tourName; }
    public void setTourName(String tourName) { this.tourName = tourName; }

    @DynamoDbAttribute("destination")
    public String getDestination() { return destination; }
    public void setDestination(String destination) { this.destination = destination; }

    @DynamoDbAttribute("hotelName")
    public String getHotelName() { return hotelName; }
    public void setHotelName(String hotelName) { this.hotelName = hotelName; }

    @DynamoDbAttribute("tourRating")
    public Double getTourRating() { return tourRating; }
    public void setTourRating(Double tourRating) { this.tourRating = tourRating; }

    @DynamoDbAttribute("startDate")
    public String getStartDate() { return startDate; }
    public void setStartDate(String startDate) { this.startDate = startDate; }

    @DynamoDbAttribute("duration")
    public String getDuration() { return duration; }
    public void setDuration(String duration) { this.duration = duration; }

    @DynamoDbAttribute("mealPlan")
    public String getMealPlan() { return mealPlan; }
    public void setMealPlan(String mealPlan) { this.mealPlan = mealPlan; }

    @DynamoDbAttribute("adults")
    public Integer getAdults() { return adults; }
    public void setAdults(Integer adults) { this.adults = adults; }

    @DynamoDbAttribute("children")
    public Integer getChildren() { return children; }
    public void setChildren(Integer children) { this.children = children; }

    @DynamoDbAttribute("freeCancelationUntil")
    public String getFreeCancelationUntil() { return freeCancelationUntil; }
    public void setFreeCancelationUntil(String freeCancelationUntil) { this.freeCancelationUntil = freeCancelationUntil; }

    @DynamoDbAttribute("status")
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @DynamoDbAttribute("createdAtEpoch")
    public Long getCreatedAtEpoch() { return createdAtEpoch; }
    public void setCreatedAtEpoch(Long createdAtEpoch) { this.createdAtEpoch = createdAtEpoch; }

    @DynamoDbAttribute("agentContact")
    public String getAgentContact() { return agentContact; }
    public void setAgentContact(String agentContact) { this.agentContact = agentContact; }

    @DynamoDbAttribute("personalDetails")
    @DynamoDbConvertedBy(PersonalDetailsConverter.class)   // <-- add this
    public List<Person> getPersonalDetails() { return personalDetails; }
    public void setPersonalDetails(List<Person> personalDetails) { this.personalDetails = personalDetails; }

    @DynamoDbAttribute("agentEmail")
    public String getAgentEmail() { return agentEmail; }
    public void setAgentEmail(String agentEmail) { this.agentEmail = agentEmail; }

    @DynamoDbAttribute("agentName")
    public String getAgentName() { return agentName; }
    public void setAgentName(String agentName) { this.agentName = agentName; }

    @DynamoDbAttribute("totalPrice")
    public Double getTotalPrice() {return totalPrice;}
    public void setTotalPrice(Double totalPrice) {this.totalPrice = totalPrice;}

    @DynamoDbAttribute("confirmedAtEpoch")
    public Long getConfirmedAtEpoch() { return confirmedAtEpoch; }
    public void setConfirmedAtEpoch(Long v) { this.confirmedAtEpoch = v; }

    @DynamoDbAttribute("customerEmail")
    public String getCustomerEmail() { return customerEmail; }
    public void setCustomerEmail(String v) { this.customerEmail = v; }

    @DynamoDbAttribute("customerPhone")
    public String getCustomerPhone() { return customerPhone; }
    public void setCustomerPhone(String v) { this.customerPhone = v; }

    public static class Person {
        private String firstName;
        private String lastName;

        @DynamoDbAttribute("firstName")
        public String getFirstName() { return firstName; }
        public void setFirstName(String firstName) { this.firstName = firstName; }

        @DynamoDbAttribute("lastName")
        public String getLastName() { return lastName; }
        public void setLastName(String lastName) { this.lastName = lastName; }
    }

    /** Converter for List<Person> <-> AttributeValue(L of M). */
    public static final class PersonalDetailsConverter implements AttributeConverter<List<Person>> {
        @Override
        public AttributeValue transformFrom(List<Person> input) {
            if (input == null || input.isEmpty()) {
                return AttributeValue.builder().l(Collections.emptyList()).build();
            }
            List<AttributeValue> list = new ArrayList<>(input.size());
            for (Person p : input) {
                if (p == null) continue;
                Map<String, AttributeValue> m = new HashMap<>();
                if (p.getFirstName() != null && !p.getFirstName().isBlank()) {
                    m.put("firstName", AttributeValue.builder().s(p.getFirstName()).build());
                }
                if (p.getLastName() != null && !p.getLastName().isBlank()) {
                    m.put("lastName", AttributeValue.builder().s(p.getLastName()).build());
                }
                list.add(AttributeValue.builder().m(m).build());
            }
            return AttributeValue.builder().l(list).build();
        }

        @Override
        public List<Person> transformTo(AttributeValue av) {
            if (av == null || av.l() == null) return Collections.emptyList();
            List<Person> out = new ArrayList<>();
            for (AttributeValue item : av.l()) {
                Map<String, AttributeValue> m = item.m();
                Person p = new Person();
                if (m != null) {
                    AttributeValue fn = m.get("firstName");
                    AttributeValue ln = m.get("lastName");
                    if (fn != null && fn.s() != null) p.setFirstName(fn.s());
                    if (ln != null && ln.s() != null) p.setLastName(ln.s());
                }
                out.add(p);
            }
            return out;
        }

        @Override
        public EnhancedType<List<Person>> type() {
            return EnhancedType.listOf(EnhancedType.of(Person.class));
        }

        @Override
        public AttributeValueType attributeValueType() {
            return AttributeValueType.L;
        }
    }

    // helpers
    @DynamoDbIgnore
    public BookingStatus getStatusEnum() { return BookingStatus.fromString(status); }

    @DynamoDbIgnore
    public void setStatusEnum(BookingStatus s) { this.status = (s == null ? null : s.name()); }
}
