package com.travelbackendapp.travelmanagement.model.api.response;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.travelbackendapp.travelmanagement.model.entity.BookingItem;
import com.travelbackendapp.travelmanagement.model.entity.DocumentRecord;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class ViewBookingDTO {

    public static ViewBookingDTO from(BookingItem booking,
                                      TravelAgent agent,
                                      TourItem tour,
                                      java.util.List<DocumentRecord> docs,
                                      java.util.function.Function<DocumentRecord, String> docUrlFn) {
        // --- image & title
        String imageUrl = null;
        List<String> imgs = (tour != null) ? tour.getImageUrls() : null;
        if (imgs != null && !imgs.isEmpty()) imageUrl = imgs.get(0);

        String title = (tour != null && tour.getName() != null && !tour.getName().isBlank())
                ? tour.getName()
                : (booking.getHotelName() != null ? booking.getHotelName() : "Tour");

        // --- date pretty + duration
        String dateStr = booking.getStartDate();
        String prettyDate = dateStr;
        try {
            prettyDate = LocalDate.parse(dateStr)
                    .format(DateTimeFormatter.ofPattern("MMM d, yyyy", Locale.US));
        } catch (Exception ignored) { }
        int days = parseDays(booking.getDuration());
        String dateWithDuration = (days > 0)
                ? String.format("%s (%d days)", prettyDate, days)
                : String.format("%s (%s)", prettyDate, nullSafe(booking.getDuration()));

        // --- guests summary line
        String guestsLine = fullNameFirst(booking)
                + adultsPart(booking.getAdults())
                + childrenPart(booking.getChildren());

        // --- price
        String totalPrice = "$0";
        if (booking.getTotalPrice() != null && booking.getTotalPrice() > 0) {
            totalPrice = money(booking.getTotalPrice());
        } else if (tour != null && tour.getPriceByDuration() != null) {
            Double p = tour.getPriceByDuration().get(booking.getDuration());
            if (p != null) totalPrice = money(p);
        }

        // --- meal
        String meal = humanMeal(booking.getMealPlan());

        // --- agent
        Agent a = (agent != null)
                ? new Agent(
                ((nullSafe(agent.getFirstName()) + " " + nullSafe(agent.getLastName())).trim()),
                agent.getEmail(),
                nullSafe(agent.getPhone()),
                nullSafe(agent.getMessenger())
        )
                : new Agent("", "", "", "");

        // =============================
        // Documents: payments + per-guest (preserve guest order from booking)
        // =============================

        // 1) Payments
        List<CustomerDocuments.FileRef> payments = new ArrayList<>();

        // 2) Start from booking personalDetails to preserve guest *order* and names
        Map<String, List<CustomerDocuments.NamedFileRef>> guestDocsByName = new LinkedHashMap<>();
        if (booking.getPersonalDetails() != null) {
            for (var p : booking.getPersonalDetails()) {
                String name = ((p.getFirstName() == null ? "" : p.getFirstName()) + " " +
                        (p.getLastName() == null ? "" : p.getLastName())).trim();
                if (name.isEmpty()) name = "guest";
                guestDocsByName.putIfAbsent(name, new ArrayList<>());
            }
        }

        // 3) Merge in uploaded docs (may include guests not in personalDetails)
        int docCount = 0;
        if (docs != null) {
            for (DocumentRecord r : docs) {
                docCount++;
                String url = (docUrlFn == null) ? "" : docUrlFn.apply(r);
                if ("PAYMENT".equalsIgnoreCase(r.getCategory())) {
                    payments.add(new CustomerDocuments.FileRef(url));
                } else if ("PASSPORT".equalsIgnoreCase(r.getCategory())) {
                    String guest = r.getGuestName();
                    guest = (guest == null || guest.trim().isEmpty()) ? "guest" : guest.trim();
                    guestDocsByName
                            .computeIfAbsent(guest, k -> new ArrayList<>())
                            .add(new CustomerDocuments.NamedFileRef(
                                    r.getFileName() == null ? "file" : r.getFileName(),
                                    url));
                }
            }
        }

        // 4) Build GuestDocuments list in the preserved order; append any extra guests (not in booking) at the end
        List<CustomerDocuments.GuestDocuments> guestDocs = new ArrayList<>();
        Set<String> alreadyAdded = new HashSet<>();
        if (booking.getPersonalDetails() != null) {
            for (var p : booking.getPersonalDetails()) {
                String name = ((p.getFirstName() == null ? "" : p.getFirstName()) + " " +
                        (p.getLastName() == null ? "" : p.getLastName())).trim();
                if (name.isEmpty()) name = "guest";
                List<CustomerDocuments.NamedFileRef> files = guestDocsByName.getOrDefault(name, Collections.emptyList());
                guestDocs.add(new CustomerDocuments.GuestDocuments(name, files));
                alreadyAdded.add(name);
            }
        }
        for (Map.Entry<String, List<CustomerDocuments.NamedFileRef>> e : guestDocsByName.entrySet()) {
            if (!alreadyAdded.contains(e.getKey())) {
                guestDocs.add(new CustomerDocuments.GuestDocuments(e.getKey(), e.getValue()));
            }
        }


        // 5) CustomerDetails + TourDetails (documents count)
        CustomerDocuments docsDto = new CustomerDocuments(payments, guestDocs);
        String customerName = fullNameFirst(booking) + adultsPart(booking.getAdults());
        String email = booking.getCustomerEmail();
        String phone = booking.getCustomerPhone();
        CustomerDetails customer = new CustomerDetails(
                customerName,
                email,
                nullSafe(phone),
                docsDto
        );

        TourDetails details = new TourDetails(
                nullSafe(booking.getTourId()),
                dateWithDuration,
                meal,
                guestsLine,
                totalPrice,
                docCount + " items"
        );

        // --- status timestamps & convenience hint
        String state = nullSafe(booking.getStatus());
        Long confirmedAt = booking.getConfirmedAtEpoch();
        Long canceledAt  = booking.getCancelledAtEpoch();
        String freeCancellationUntil = booking.getFreeCancelationUntil();
        String duration = booking.getDuration();


        // --- build final DTO
        return new ViewBookingDTO(
                booking.getBookingId(),
                state,
                imageUrl,
                title,
                nullSafe(booking.getDestination()),
                details,
                a,
                customer,
                booking.getCancelledBy(),
                booking.getCancellationReason(),
                canceledAt,
                confirmedAt,
                freeCancellationUntil,
                duration
        );
    }

    // ---------- helpers ----------

    private static String nullSafe(String s) { return s == null ? "" : s; }

    private static int parseDays(String durationStr) {
        if (durationStr == null) return -1;
        String s = durationStr.trim().toLowerCase(Locale.ROOT);
        int i = 0; while (i < s.length() && !Character.isDigit(s.charAt(i))) i++;
        int j = i; while (j < s.length() && Character.isDigit(s.charAt(j))) j++;
        if (i < j) {
            try { return Integer.parseInt(s.substring(i, j)); } catch (NumberFormatException ignored) {}
        }
        return -1;
    }

    private static String money(Double v) {
        if (v == null) return "$0";
        if (Math.floor(v) == v) return "$" + String.format(Locale.US, "%.0f", v);
        return "$" + String.format(Locale.US, "%.2f", v);
    }

    private static String humanMeal(String code) {
        if (code == null) return "";
        switch (code.toUpperCase(Locale.ROOT)) {
            case "BB": return "Breakfast (BB)";
            case "HB": return "Half-board (HB)";
            case "FB": return "Full-board (FB)";
            case "AI": return "All inclusive (AI)";
            default:   return code;
        }
    }

    private static String fullNameFirst(BookingItem b) {
        if (b.getPersonalDetails() == null || b.getPersonalDetails().isEmpty()) return "Guest";
        var p = b.getPersonalDetails().get(0);
        String fn = p.getFirstName() == null ? "" : p.getFirstName();
        String ln = p.getLastName() == null ? "" : p.getLastName();
        return (fn + " " + ln).trim();
    }

    private static String adultsPart(Integer adults) {
        if (adults == null || adults <= 0) return "";
        return (adults == 1) ? " (1 adult)" : " (" + adults + " adults)";
    }

    private static String childrenPart(Integer children) {
        if (children == null || children <= 0) return "";
        return " (" + children + " children)";
    }

    @SuppressWarnings("unused")
    private static List<CustomerDocuments.GuestDocuments> buildGuestDocs(BookingItem b) {
        if (b.getPersonalDetails() == null || b.getPersonalDetails().isEmpty()) return Collections.emptyList();
        List<CustomerDocuments.GuestDocuments> out = new ArrayList<>();
        for (var p : b.getPersonalDetails()) {
            String name = ((p.getFirstName() == null ? "" : p.getFirstName()) + " " +
                    (p.getLastName()  == null ? "" : p.getLastName())).trim();
            out.add(new CustomerDocuments.GuestDocuments(name, Collections.emptyList()));
        }
        return out;
    }

    // ---------- POJO (Jackson) ----------

    public ViewBookingDTO(String id,
                          String state,
                          String tourImageUrl,
                          String name,
                          String destination,
                          TourDetails tourDetails,
                          Agent travelAgent,
                          CustomerDetails customerDetails,
                          String cancelledBy,
                          String cancelReason,
                          Long canceledAtEpoch,
                          Long confirmedAtEpoch,
                          String freeCancellationUntil,
                          String duration
    ) {
        this.id = id;
        this.state = state;
        this.tourImageUrl = tourImageUrl;
        this.name = name;
        this.destination = destination;
        this.tourDetails = tourDetails;
        this.travelAgent = travelAgent;
        this.customerDetails = customerDetails;
        this.cancelledBy = cancelledBy;
        this.cancelReason = cancelReason;
        this.canceledAtEpoch = canceledAtEpoch;
        this.confirmedAtEpoch = confirmedAtEpoch;
        this.freeCancellationUntil = freeCancellationUntil;
        this.duration = duration;
    }

    @JsonProperty("id") private String id;
    @JsonProperty("state") private String state;
    @JsonProperty("tourImageUrl") private String tourImageUrl;
    @JsonProperty("name") private String name;
    @JsonProperty("destination") private String destination;

    @JsonProperty("tourDetails") private TourDetails tourDetails;
    @JsonProperty("travelAgent") private Agent travelAgent;

    @JsonProperty("customerDetails") private CustomerDetails customerDetails;

    @JsonProperty("canceledBy") private String cancelledBy; // Swagger uses "canceledBy"
    @JsonProperty("cancelReason") private String cancelReason;

    @JsonProperty("confirmedAtEpoch") private Long confirmedAtEpoch;   // nullable
    @JsonProperty("canceledAtEpoch")  private Long canceledAtEpoch;    // nullable

    @JsonProperty("freeCancellationUntil") private String freeCancellationUntil;
    @JsonProperty("duration") private String duration;

    public static class TourDetails {
        public TourDetails(String tourId, String date, String mealPlans, String guests, String totalPrice, String documents) {
            this.tourId = tourId;
            this.date = date;
            this.mealPlans = mealPlans;
            this.guests = guests;
            this.totalPrice = totalPrice;
            this.documents = documents;
        }
        @JsonProperty("tourId") public String tourId;
        @JsonProperty("date") public String date;
        @JsonProperty("mealPlans") public String mealPlans;
        @JsonProperty("guests") public String guests;
        @JsonProperty("totalPrice") public String totalPrice;
        @JsonProperty("documents") public String documents;
    }

    public static class Agent {
        public Agent(String name, String email, String phone, String messenger) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.messenger = messenger;
        }
        @JsonProperty("name") public String name;
        @JsonProperty("email") public String email;
        @JsonProperty("phone") public String phone;
        @JsonProperty("messenger") public String messenger;
    }

    public static class CustomerDetails {
        public CustomerDetails(String name, String email, String phone, CustomerDocuments documents) {
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.documents = documents;
        }
        @JsonProperty("name") public String name;
        @JsonProperty("email") public String email;
        @JsonProperty("phone") public String phone;
        @JsonProperty("documents") public CustomerDocuments documents;
    }

    public static class CustomerDocuments {
        public CustomerDocuments(List<FileRef> payments, List<GuestDocuments> guestDocuments) {
            this.payments = payments;
            this.guestDocuments = guestDocuments;
        }
        @JsonProperty("payments") public List<FileRef> payments;
        @JsonProperty("guestDocuments") public List<GuestDocuments> guestDocuments;

        public static class FileRef {
            public FileRef(String fileUrl) { this.fileUrl = fileUrl; }
            @JsonProperty("fileUrl") public String fileUrl;
        }

        public static class NamedFileRef {
            public NamedFileRef(String fileName, String fileUrl) {
                this.fileName = fileName; this.fileUrl = fileUrl;
            }
            @JsonProperty("fileName") public String fileName;
            @JsonProperty("fileUrl") public String fileUrl;
        }

        public static class GuestDocuments {
            public GuestDocuments(String userName, List<NamedFileRef> documents) {
                this.userName = userName; this.documents = documents;
            }
            @JsonProperty("userName") public String userName;
            @JsonProperty("documents") public List<NamedFileRef> documents;
        }
    }
}
