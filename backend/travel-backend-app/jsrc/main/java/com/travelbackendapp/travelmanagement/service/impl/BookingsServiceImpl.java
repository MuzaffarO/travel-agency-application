package com.travelbackendapp.travelmanagement.service.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.domain.BookingStatus;
import com.travelbackendapp.travelmanagement.model.api.request.CreateBookingRequest;
import com.travelbackendapp.travelmanagement.model.api.response.CreateBookingResponse;
import com.travelbackendapp.travelmanagement.model.api.response.ListDocumentsResponse;
import com.travelbackendapp.travelmanagement.model.api.response.ViewBookingDTO;
import com.travelbackendapp.travelmanagement.model.entity.BookingItem;
import com.travelbackendapp.travelmanagement.model.entity.DocumentRecord;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.*;
import com.travelbackendapp.travelmanagement.service.BookingEventPublisher;
import com.travelbackendapp.travelmanagement.service.BookingsService;
import com.travelbackendapp.travelmanagement.util.HttpResponses;
import com.travelbackendapp.travelmanagement.util.S3DocumentsStorage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.travelbackendapp.travelmanagement.model.api.request.UploadDocumentsRequest;
import com.travelbackendapp.travelmanagement.model.api.response.UploadDocumentsResponse;


import javax.inject.Inject;
import javax.inject.Named;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeParseException;
import java.util.*;
import java.util.stream.Collectors;

import static com.travelbackendapp.travelmanagement.util.RequestUtils.isBlank;
import static java.nio.charset.StandardCharsets.UTF_8;

public class BookingsServiceImpl implements BookingsService {
    private static final Logger log = LoggerFactory.getLogger(BookingsServiceImpl.class);

    private final ObjectMapper mapper;
    private final ToursRepository toursRepo;
    private final BookingsRepository bookingsRepo;
    private final TravelAgentRepository agentsRepo;
    private final BookingsStatusRepository bookingsStatusRepo;
    private final BookingEventPublisher eventPublisher;
    private final S3DocumentsStorage s3Docs;
    private final DocumentsRepository documentsRepo;
    private final String bookingDocsBucket;
    private final String awsRegion;


    @Inject
    public BookingsServiceImpl(ObjectMapper mapper, ToursRepository toursRepo, BookingsRepository bookingsRepo, TravelAgentRepository agentsRepo,
                               BookingsStatusRepository bookingsStatusRepository, BookingEventPublisher eventPublisher, S3DocumentsStorage s3Docs,
                               DocumentsRepository documentsRepo, @Named("BOOKING_DOCS_BUCKET") String bookingDocsBucket,
                               @Named("AWS_REGION") String awsRegion) {
        this.mapper = mapper;
        this.toursRepo = toursRepo;
        this.bookingsRepo = bookingsRepo;
        this.agentsRepo = agentsRepo;
        this.eventPublisher = eventPublisher;
        this.bookingsStatusRepo = bookingsStatusRepository;
        this.s3Docs = s3Docs;
        this.documentsRepo = documentsRepo;
        this.bookingDocsBucket = bookingDocsBucket;
        this.awsRegion = awsRegion;
    }

    @Override
    public APIGatewayProxyResponseEvent create(APIGatewayProxyRequestEvent event) {
        try {
            // ---- Auth: require Cognito claims via API Gateway authorizer ----
            String userId = extractClaim(event, "sub");                // stable user identifier
            if (userId == null) {
                // No claims → user not logged in (or route not protected)
                return HttpResponses.error(mapper, 401, "authentication required: please log in or sign up");
            }

            var body = mapper.readValue(event.getBody(), CreateBookingRequest.class);

            if (isBlank(body.tourId) || isBlank(body.date)
                    || isBlank(body.duration) || isBlank(body.mealPlan)
                    || body.guests == null || body.personalDetails == null || body.personalDetails.isEmpty()) {
                return HttpResponses.error(mapper, 400, "invalid booking payload");
            }

            LocalDate start;
            try {
                start = LocalDate.parse(body.date);
            } catch (DateTimeParseException e) {
                return HttpResponses.error(mapper, 400, "date must be ISO yyyy-MM-dd");
            }

            // Load tour
            var opt = toursRepo.getById(body.tourId);
            if (opt.isEmpty()) return HttpResponses.error(mapper, 404, "tour not found");
            TourItem tour = opt.get();

            String agentEmail = tour.getAgentEmail();
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                return HttpResponses.error(mapper, 409, "tour has no assigned travel agent");
            }
            var agent = agentsRepo.findByEmail(agentEmail);
            if (agent == null || agent.getRole() == null || !"TRAVEL_AGENT".equals(agent.getRole())) {
                return HttpResponses.error(mapper, 409, "assigned travel agent is not available");
            }

            // Capacity presence check
            if (tour.getAvailablePackages() != null && tour.getAvailablePackages() <= 0) {
                return HttpResponses.error(mapper, 409, "tour is fully booked");
            }

            // Start date checks
            if (start.isBefore(LocalDate.now())) {
                return HttpResponses.error(mapper, 400, "start date cannot be in the past");
            }
            if (tour.getStartDates() != null && !tour.getStartDates().isEmpty()) {
                if (!tour.getStartDates().contains(start.toString())) {
                    return HttpResponses.error(mapper, 400, "selected start date is not available for this tour");
                }
            }

            String canonicalDurationKey = matchDurationKey(body.duration, tour.getDurations());
            if (canonicalDurationKey == null) {
                return HttpResponses.error(mapper, 400, "selected duration is not available for this tour");
            }
            int durationDays = parseDays(canonicalDurationKey);

            String mealCode = normalizeMealPlanToCode(body.mealPlan);
            if (!mealPlanOffered(mealCode, tour.getMealPlans())) {
                return HttpResponses.error(mapper, 400, "selected meal plan is not available for this tour");
            }

            // Seat count
            int adults = Math.max(0, body.guests.adult);
            int children = Math.max(0, body.guests.children);
            int seats = adults + children;
            if (seats <= 0) {
                return HttpResponses.error(mapper, 400, "at least one guest is required");
            }
            Integer avail = tour.getAvailablePackages();
            if (avail != null && avail < seats) {
                return HttpResponses.error(mapper, 409, "not enough capacity for the selected number of guests, only " + avail + " seats left");
            }

            Map<String, Double> priceMap = tour.getPriceByDuration();
            Double basePerPerson = null;
            if (priceMap != null && !priceMap.isEmpty()) {
                basePerPerson = priceMap.get(canonicalDurationKey);
                if (basePerPerson == null) {
                    for (Map.Entry<String, Double> e : priceMap.entrySet()) {
                        if (parseDays(e.getKey()) == durationDays) {
                            basePerPerson = e.getValue();
                            break;
                        }
                    }
                }
            }
            if (basePerPerson == null) {
                Double p = tour.getPriceFrom();
                if (p > 0) basePerPerson = p;
            }
            if (basePerPerson == null) {
                return HttpResponses.error(mapper, 400, "selected duration has no price configured");
            }
            Map<String, Double> suppMap = tour.getMealSupplementsPerDay();
            double supplementPerDayPerPerson = (suppMap != null && suppMap.get(mealCode) != null) ? suppMap.get(mealCode) : 0.0;
            double total = basePerPerson * seats + (supplementPerDayPerPerson * durationDays * seats);

            // Free cancellation date
            int daysBefore = tour.getFreeCancellationDaysBefore() != null ? tour.getFreeCancellationDaysBefore() : 10;
            String freeUntil = start.minusDays(Math.max(daysBefore, 0)).toString();

            String today = LocalDate.now().toString();
            String bookingId = today + "_" + UUID.randomUUID();

            String agentName = ((agent.getFirstName() == null ? "" : agent.getFirstName().trim()) + " " +
                    (agent.getLastName() == null ? "" : agent.getLastName().trim())).trim();

            String callerEmail = extractClaim(event, "email");
            String callerPhone = extractClaim(event, "phone_number");

            BookingItem b = new BookingItem();
            b.setUserId(userId);
            b.setBookingId(bookingId);
            b.setTourId(tour.getTourId());
            b.setTourName(tour.getName());
            b.setDestination(tour.getDestination());
            b.setHotelName(tour.getHotelName());
            b.setTourRating(tour.getRating());
            b.setStartDate(start.toString());
            b.setDuration(canonicalDurationKey);
            b.setMealPlan(mealCode);
            b.setAdults(adults);
            b.setChildren(children);
            b.setAgentEmail(agentEmail);
            b.setTotalPrice(total);
            b.setCustomerEmail(callerEmail);
            b.setCustomerPhone(callerPhone);
            b.setAgentName(agentName.isEmpty() ? agentEmail : agentName);
            var persons = body.personalDetails.stream()
                    .map(p -> {
                        BookingItem.Person bp = new BookingItem.Person();
                        bp.setFirstName(p.firstName.trim());
                        bp.setLastName(p.lastName.trim());
                        return bp;
                    })
                    .collect(Collectors.toList());
            b.setPersonalDetails(persons);
            b.setFreeCancelationUntil(freeUntil);
            b.setStatusEnum(BookingStatus.BOOKED);
            b.setCreatedAtEpoch(System.currentTimeMillis());

            // Atomically reserve all seats and save booking
            try {
                bookingsRepo.transactReserveSeatsAndSave(b, tour.getTourId(), seats);
            } catch (software.amazon.awssdk.services.dynamodb.model.TransactionCanceledException tce) {
                return HttpResponses.error(mapper, 409, "not enough capacity left");
            }

            String details = buildConfirmationText(tour.getHotelName(), start, canonicalDurationKey, mealCode, adults, children);

            CreateBookingResponse resp = new CreateBookingResponse(bookingId, freeUntil, details);
            resp.totalPrice = money(total);
            resp.breakdown = new CreateBookingResponse.PriceBreakdown(
                    money(basePerPerson), durationDays, seats, money(supplementPerDayPerPerson)
            );

            return HttpResponses.json(mapper, 201, resp);

        } catch (Exception e) {
            log.error("create booking failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent view(APIGatewayProxyRequestEvent event) {
        try {
            // ---- Auth ----
            String callerSub = extractClaim(event, "sub");
            String callerRole = extractClaim(event, "custom:role");
            String callerEmail = extractClaim(event, "email");

            if (callerSub == null) {
                return HttpResponses.error(mapper, 401, "authentication required: please log in or sign up");
            }
            if (callerRole == null) callerRole = "";

            final List<ViewBookingDTO> result = new ArrayList<>();

            // helper to build a stable, API-local URL we can implement later
            java.util.function.BiFunction<String, String, String> docDownloadUrl =
                    (bookingId, docId) -> "/bookings/" + urlEncode(bookingId) + "/documents/" + urlEncode(docId);

            if ("CUSTOMER".equalsIgnoreCase(callerRole)) {
                // Own bookings
                List<BookingItem> bookingItems = bookingsRepo.findByUserId(callerSub);
                for (BookingItem booking : bookingItems) {
                    TourItem tour = toursRepo.getById(booking.getTourId()).orElse(null);
                    String agentEmail = booking.getAgentEmail();
                    if (agentEmail == null && tour != null) agentEmail = tour.getAgentEmail();
                    TravelAgent agent = (agentEmail != null) ? agentsRepo.findByEmail(agentEmail) : null;

                    // fetch documents for this booking
                    List<DocumentRecord> docs = documentsRepo.listByBookingId(booking.getBookingId());

                    ViewBookingDTO dto = ViewBookingDTO.from(
                            booking,
                            agent,
                            tour,
                            docs,
                            r -> s3HttpUrl(r.getS3Key())
                    );
                    result.add(dto);
                }
                return HttpResponses.json(mapper, 200, java.util.Map.of("bookings", result));
            }

            if ("TRAVEL_AGENT".equalsIgnoreCase(callerRole) || "ADMIN".equalsIgnoreCase(callerRole)) {
                if (callerEmail == null || callerEmail.isBlank()) {
                    return HttpResponses.error(mapper, 403, "missing email claim");
                }

                TravelAgent agent = agentsRepo.findByEmail(callerEmail);
                if (agent == null || (!"TRAVEL_AGENT".equals(agent.getRole()) && !"ADMIN".equals(agent.getRole()))) {
                    return HttpResponses.error(mapper, 403, "not a registered travel agent or admin");
                }

                // TRAVEL_AGENT sees only their bookings, ADMIN sees all bookings
                List<BookingItem> bookingItems;
                if ("ADMIN".equalsIgnoreCase(callerRole)) {
                    // Admin sees all bookings - get all bookings
                    bookingItems = bookingsRepo.findAll();
                } else {
                    // Travel agent sees only their bookings
                    bookingItems = bookingsRepo.findByAgentEmail(callerEmail);
                }

                for (BookingItem booking : bookingItems) {
                    TourItem tour = toursRepo.getById(booking.getTourId()).orElse(null);

                    // fetch documents
                    List<DocumentRecord> docs = documentsRepo.listByBookingId(booking.getBookingId());

                    ViewBookingDTO dto = ViewBookingDTO.from(
                            booking,
                            agent,
                            tour,
                            docs,
                            r -> s3HttpUrl(r.getS3Key())
                    );
                    result.add(dto);
                }
                return HttpResponses.json(mapper, 200, java.util.Map.of("bookings", result));
            }

            return HttpResponses.error(mapper, 403, "incorrect role: must be either customer, travel agent, or admin");
        } catch (Exception e) {
            log.error("view booking failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    // URL-encode for the synthetic links we embed in the DTO
    private static String urlEncode(String s) {
        try {
            return java.net.URLEncoder.encode(s == null ? "" : s, UTF_8.name());
        } catch (Exception ignored) {
            return s;
        }
    }


    @Override
    public APIGatewayProxyResponseEvent update(APIGatewayProxyRequestEvent event, String bookingId) {
        try {
            // ---- Auth ----
            String callerSub   = extractClaim(event, "sub");
            String callerRole  = extractClaim(event, "custom:role");
            String callerEmail = extractClaim(event, "email");

            if (callerSub == null) {
                return HttpResponses.error(mapper, 401, "authentication required: please log in or sign up");
            }
            if (isBlank(bookingId)) {
                return HttpResponses.error(mapper, 400, "bookingId is required");
            }
            if (callerRole == null) callerRole = "";

            // ---- Authorization & booking load (customer: own PK; agent: by bookingId + assignment) ----
            BookingItem existing;
            if ("CUSTOMER".equalsIgnoreCase(callerRole)) {
                existing = bookingsRepo.get(callerSub, bookingId);
                if (existing == null) {
                    return HttpResponses.error(mapper, 404, "booking not found");
                }
            } else if ("TRAVEL_AGENT".equalsIgnoreCase(callerRole)) {
                if (callerEmail == null || callerEmail.isBlank()) {
                    return HttpResponses.error(mapper, 403, "missing email claim");
                }
                TravelAgent agent = agentsRepo.findByEmail(callerEmail);
                if (agent == null || !"TRAVEL_AGENT".equals(agent.getRole())) {
                    return HttpResponses.error(mapper, 403, "not a registered travel agent");
                }
                existing = bookingsRepo.getByBookingId(bookingId);
                if (existing == null) {
                    return HttpResponses.error(mapper, 404, "booking not found");
                }
                if (existing.getAgentEmail() == null || !existing.getAgentEmail().equalsIgnoreCase(callerEmail)) {
                    return HttpResponses.error(mapper, 403, "not the assigned travel agent");
                }
            } else {
                return HttpResponses.error(mapper, 403, "incorrect role: must be either customer or travel agent");
            }

            // ---- Terminal state checks ----
            var st = existing.getStatusEnum();
            if (st == BookingStatus.CANCELLED)
                return HttpResponses.error(mapper, 409, "canceled booking cannot be modified");
            if (st == BookingStatus.FINISHED)
                return HttpResponses.error(mapper, 409, "finished booking cannot be modified");
            if (st == BookingStatus.CONFIRMED)
                return HttpResponses.error(mapper, 409, "confirmed booking cannot be modified");

            // ---- Parse request ----
            var body = mapper.readValue(event.getBody(), CreateBookingRequest.class);
            if (body == null
                    || isBlank(body.date) || isBlank(body.duration) || isBlank(body.mealPlan)
                    || body.guests == null || body.personalDetails == null || body.personalDetails.isEmpty()) {
                return HttpResponses.error(mapper, 400, "invalid booking payload");
            }

            // Do not allow changing tour
            if (!isBlank(body.tourId) && !body.tourId.equals(existing.getTourId())) {
                return HttpResponses.error(mapper, 400, "changing tour is not supported");
            }
            String tourId = existing.getTourId();

            // ---- Load tour snapshot for validation/pricing ----
            var tourOpt = toursRepo.getById(tourId);
            if (tourOpt.isEmpty()) return HttpResponses.error(mapper, 404, "tour not found");
            TourItem tour = tourOpt.get();

            // Validate agent still assigned on tour (for pricing/ownership sanity)
            String agentEmail = tour.getAgentEmail();
            if (agentEmail == null || agentEmail.trim().isEmpty()) {
                return HttpResponses.error(mapper, 409, "tour has no assigned travel agent");
            }

            // ---- Parse & validate date ----
            LocalDate start;
            try {
                start = LocalDate.parse(body.date);
            } catch (DateTimeParseException e) {
                return HttpResponses.error(mapper, 400, "date must be ISO yyyy-MM-dd");
            }
            if (start.isBefore(LocalDate.now())) {
                return HttpResponses.error(mapper, 400, "start date cannot be in the past");
            }
            if (tour.getStartDates() != null && !tour.getStartDates().isEmpty()
                    && !tour.getStartDates().contains(start.toString())) {
                return HttpResponses.error(mapper, 400, "selected start date is not available for this tour");
            }

            // ---- Duration & meal-plan ----
            String canonicalDurationKey = matchDurationKey(body.duration, tour.getDurations());
            if (canonicalDurationKey == null) {
                return HttpResponses.error(mapper, 400, "selected duration is not available for this tour");
            }
            int durationDays = parseDays(canonicalDurationKey);

            String mealCode = normalizeMealPlanToCode(body.mealPlan);
            if (!mealPlanOffered(mealCode, tour.getMealPlans())) {
                return HttpResponses.error(mapper, 400, "selected meal plan is not available for this tour");
            }

            // ---- Seats and capacity delta ----
            int newAdults = Math.max(0, body.guests.adult);
            int newChildren = Math.max(0, body.guests.children);
            int newSeats = newAdults + newChildren;
            if (newSeats <= 0) {
                return HttpResponses.error(mapper, 400, "at least one guest is required");
            }
            int oldSeats = (existing.getAdults() == null ? 0 : existing.getAdults())
                    + (existing.getChildren() == null ? 0 : existing.getChildren());
            int delta = newSeats - oldSeats;

            if (delta > 0) {
                try {
                    bookingsRepo.adjustTourCapacity(tourId, delta); // consume extra seats
                } catch (software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException e) {
                    Integer avail = tour.getAvailablePackages();
                    String msg = "not enough capacity left";
                    if (avail != null && avail >= 0) {
                        msg = "not enough capacity for the selected number of guests, only " + avail + " seats left";
                    }
                    return HttpResponses.error(mapper, 409, msg);
                }
            } else if (delta < 0) {
                bookingsRepo.adjustTourCapacity(tourId, delta); // return seats
            }

            // ---- Price calc ----
            Map<String, Double> priceMap = tour.getPriceByDuration();
            Double basePerPerson = null;
            if (priceMap != null && !priceMap.isEmpty()) {
                basePerPerson = priceMap.get(canonicalDurationKey);
                if (basePerPerson == null) {
                    for (Map.Entry<String, Double> e : priceMap.entrySet()) {
                        if (parseDays(e.getKey()) == durationDays) {
                            basePerPerson = e.getValue();
                            break;
                        }
                    }
                }
            }
            if (basePerPerson == null) {
                Double p = tour.getPriceFrom();
                if (p != null && p > 0) basePerPerson = p;
            }
            if (basePerPerson == null) {
                return HttpResponses.error(mapper, 400, "selected duration has no price configured");
            }

            Map<String, Double> suppMap = tour.getMealSupplementsPerDay();
            double supplementPerDayPerPerson =
                    (suppMap != null && suppMap.get(mealCode) != null) ? suppMap.get(mealCode) : 0.0;
            double total = basePerPerson * newSeats + (supplementPerDayPerPerson * durationDays * newSeats);

            // ---- Recompute free-cancel date ----
            int daysBefore = tour.getFreeCancellationDaysBefore() != null ? tour.getFreeCancellationDaysBefore() : 10;
            String freeUntil = start.minusDays(Math.max(daysBefore, 0)).toString();

            // ---- Update & persist ----
            existing.setStartDate(start.toString());
            existing.setDuration(canonicalDurationKey);
            existing.setMealPlan(mealCode);
            existing.setAdults(newAdults);
            existing.setChildren(newChildren);
            existing.setTotalPrice(total);
            existing.setPersonalDetails(body.personalDetails.stream().map(p -> {
                BookingItem.Person bp = new BookingItem.Person();
                bp.setFirstName(p.firstName.trim());
                bp.setLastName(p.lastName.trim());
                return bp;
            }).collect(Collectors.toList()));
            existing.setFreeCancelationUntil(freeUntil);
            // keep status / createdAtEpoch / confirmedAtEpoch

            bookingsRepo.put(existing);

            // ---- Response (same shape as create) ----
            String details = buildConfirmationText(
                    tour.getHotelName(), start, canonicalDurationKey, mealCode, newAdults, newChildren
            );

            CreateBookingResponse resp = new CreateBookingResponse(bookingId, freeUntil, details);
            resp.totalPrice = money(total);
            resp.breakdown = new CreateBookingResponse.PriceBreakdown(
                    money(basePerPerson), durationDays, newSeats, money(supplementPerDayPerPerson)
            );

            return HttpResponses.json(mapper, 200, resp);

        } catch (Exception e) {
            log.error("update booking failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }


    @Override
    public APIGatewayProxyResponseEvent confirm(APIGatewayProxyRequestEvent event, String bookingId) {
        try {
            String role = extractClaim(event, "custom:role");
            String email = extractClaim(event, "email");
            if (!"TRAVEL_AGENT".equalsIgnoreCase(role) || email == null) {
                return HttpResponses.error(mapper, 403, "only travel agents can confirm bookings");
            }
            if (isBlank(bookingId)) return HttpResponses.error(mapper, 400, "bookingId is required");

            BookingItem b = bookingsRepo.getByBookingId(bookingId);
            if (b == null) return HttpResponses.error(mapper, 404, "booking not found");

            // verify assigned agent
            if (b.getAgentEmail() == null || !b.getAgentEmail().equalsIgnoreCase(email)) {
                return HttpResponses.error(mapper, 403, "not the assigned travel agent");
            }

            var st = b.getStatusEnum();
            if (st == BookingStatus.CONFIRMED) {
                return HttpResponses.json(mapper, 200, Map.of("message", "Booking has already been confirmed before"));
            }
            if (st != BookingStatus.BOOKED) {
                return HttpResponses.error(mapper, 409, "only bookings in BOOKED status can be confirmed");
            }

            bookingsStatusRepo.markConfirmed(b.getUserId(), b.getBookingId(), email);
            
            // Publish CONFIRM event to SQS
            try {
                eventPublisher.publishBookingEvent(
                    "CONFIRM",
                    b.getBookingId(),
                    b.getUserId(),
                    b.getTourId(),
                    b.getAgentEmail()
                );
                log.info("Published CONFIRM event for booking {}", b.getBookingId());
            } catch (Exception e) {
                log.error("Failed to publish CONFIRM event for booking {}: {}", b.getBookingId(), e.getMessage(), e);
                // Don't fail the confirmation if event publishing fails
            }
            
            return HttpResponses.json(mapper, 200, Map.of("message", "Booking confirmed successfully."));


        } catch (software.amazon.awssdk.services.dynamodb.model.ConditionalCheckFailedException e) {
            return HttpResponses.error(mapper, 409, "cannot confirm: preconditions failed");
        } catch (Exception e) {
            log.error("confirm booking failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }


    private static String buildConfirmationText(String hotelName, LocalDate start, String duration,
                                                String mealPlan, int adults, int children) {
        int totalGuests = adults + children;
        String ppl = (totalGuests == 1) ? "1 adult" : totalGuests + " guests";

        String m;
        switch (mealPlan) {
            case "BB":
                m = "Breakfast (BB)";
                break;
            case "HB":
                m = "Half-board (HB)";
                break;
            case "FB":
                m = "Full-board (FB)";
                break;
            case "AI":
                m = "All inclusive (AI)";
                break;
            default:
                m = mealPlan;
        }

        String hotel = (hotelName == null || hotelName.isBlank()) ? "the selected hotel" : hotelName;

        return String.format(
                "You have booked a tour at %s, starting date %s (%s), %s for %s successfully. " +
                        "Please upload your travel documents to the booking on the 'My Tours' page and wait for the Travel Agent to contact you.",
                hotel, start, duration, m, ppl
        );
    }

    // ---- helpers ----

    private static int parseDays(String durationStr) {
        if (durationStr == null) return -1;
        String s = durationStr.trim().toLowerCase(Locale.ROOT);
        int i = 0;
        while (i < s.length() && !Character.isDigit(s.charAt(i))) i++;
        int j = i;
        while (j < s.length() && Character.isDigit(s.charAt(j))) j++;
        if (i < j) {
            try {
                return Integer.parseInt(s.substring(i, j));
            } catch (NumberFormatException ignored) {
            }
        }
        return -1;
    }

    /**
     * Find canonical duration key (e.g., "7 days") that matches requested days.
     */
    private static String matchDurationKey(String requestedDuration, List<String> offeredDurations) {
        if (offeredDurations == null || offeredDurations.isEmpty()) return null;
        int want = parseDays(requestedDuration);
        if (want <= 0) return null;
        for (String key : offeredDurations) {
            if (parseDays(key) == want) return key;
        }
        return null;
    }

    /**
     * Normalize meal plan to code (BB/HB/FB/AI). Accepts "BB" or "Breakfast (BB)".
     */
    private static String normalizeMealPlanToCode(String raw) {
        if (raw == null) return null;
        String s = raw.trim();
        int li = s.lastIndexOf('(');
        int ri = s.lastIndexOf(')');
        if (li >= 0 && ri > li + 1) {
            String code = s.substring(li + 1, ri).trim();
            if (!code.isEmpty()) return code;
        }
        return s.toUpperCase(Locale.ROOT);
    }

    private static boolean mealPlanOffered(String code, List<String> tourMealPlanCodes) {
        if (code == null) return false;
        if (tourMealPlanCodes == null || tourMealPlanCodes.isEmpty()) return false;
        for (String offered : tourMealPlanCodes) {
            if (code.equalsIgnoreCase(offered)) return true;
        }
        return false;
    }

    private static String money(Double v) {
        if (v == null) return "$0";
        if (Math.floor(v) == v) return "$" + String.format(Locale.US, "%.0f", v);
        return "$" + String.format(Locale.US, "%.2f", v);
    }

    @Override
    public APIGatewayProxyResponseEvent cancel(APIGatewayProxyRequestEvent event, String bookingId) {
        try {
            String userId = extractClaim(event, "sub");
            String role = extractClaim(event, "custom:role");
            String email = extractClaim(event, "email");

            if (userId == null)
                return HttpResponses.error(mapper, 401, "authentication required: please log in or sign up");
            if (isBlank(bookingId)) return HttpResponses.error(mapper, 400, "bookingId is required");

            final boolean isAgent = "TRAVEL_AGENT".equalsIgnoreCase(role);

            // Load booking: customers only their own, agents by id (then verify assignment)
            BookingItem existing = isAgent ? bookingsRepo.getByBookingId(bookingId)
                    : bookingsRepo.get(userId, bookingId);
            if (existing == null) return HttpResponses.error(mapper, 404, "booking not found");

            if (isAgent) {
                if (email == null || existing.getAgentEmail() == null ||
                        !existing.getAgentEmail().equalsIgnoreCase(email)) {
                    return HttpResponses.error(mapper, 403, "not the assigned travel agent");
                }
            } else if (!userId.equals(existing.getUserId())) {
                return HttpResponses.error(mapper, 403, "forbidden");
            }

            // terminal checks
            var st = existing.getStatusEnum();
            if (st == BookingStatus.CANCELLED) return HttpResponses.error(mapper, 409, "booking is already cancelled");
            if (st == BookingStatus.FINISHED)
                return HttpResponses.error(mapper, 409, "cannot cancel a finished booking");

            // dates
            LocalDate startDate = LocalDate.parse(existing.getStartDate());
            LocalDate freeUntil = LocalDate.parse(existing.getFreeCancelationUntil());
            LocalDate now = LocalDate.now();

            // Parse optional body
            Map<String, Object> body = event.getBody() == null ? Map.of() : mapper.readValue(event.getBody(), Map.class);
            String reason = (String) body.getOrDefault("cancellationReason", null);
            String comment = (String) body.getOrDefault("comment", null);

            // Pricing policy
            boolean isFree = !now.isAfter(freeUntil);               // on or before freeUntil => free
            double total = existing.getTotalPrice() == null ? 0.0 : existing.getTotalPrice();
            double fee = isFree ? 0.0 : total;
            double refund = isFree ? total : 0.0;

            // Persist cancellation
            existing.setCancellationReason(reason);
            existing.setCancellationComment(comment);
            existing.setCancelledAtEpoch(System.currentTimeMillis());
            existing.setStatusEnum(BookingStatus.CANCELLED);
            existing.setCancelledBy(isAgent ? "TRAVEL_AGENT" : "CUSTOMER");
            bookingsRepo.put(existing);

            // Publish cancellation event to SQS
            try {
                eventPublisher.publishBookingEvent(
                        "CANCEL",
                        bookingId,
                        existing.getUserId(),
                        existing.getTourId(),
                        existing.getAgentEmail()
                );
                log.info("Published CANCEL event for booking {}", bookingId);
            } catch (Exception e) {
                log.error("Failed to publish CANCEL event for booking {}: {}", bookingId, e.getMessage(), e);
                // Don't fail the cancellation if event publishing fails
            }

            // Return seats to capacity if tour is in the future
            int seats = (existing.getAdults() == null ? 0 : existing.getAdults())
                    + (existing.getChildren() == null ? 0 : existing.getChildren());
            if (seats > 0 && now.isBefore(startDate)) {
                bookingsRepo.adjustTourCapacity(existing.getTourId(), -seats);
            }

            Map<String, Object> resp = new LinkedHashMap<>();
            resp.put("bookingId", bookingId);
            resp.put("status", BookingStatus.CANCELLED.name());
            resp.put("cancellationFee", fee);
            resp.put("refundAmount", refund);
            resp.put("cancelledAt", LocalDateTime.now().toString());
            resp.put("message", isFree
                    ? "Booking cancelled successfully with no fees"
                    : "Booking cancelled. Free cancellation period is over, charges are non-refundable");

            return HttpResponses.json(mapper, 200, resp);

        } catch (Exception e) {
            log.error("cancel booking failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent uploadDocuments(APIGatewayProxyRequestEvent event, String bookingId) {
        try {
            // ---- Auth ----
            String callerSub   = extractClaim(event, "sub");
            String callerRole  = extractClaim(event, "custom:role");
            String callerEmail = extractClaim(event, "email");

            if (callerSub == null) {
                return HttpResponses.error(mapper, 401, "authentication required: please log in or sign up");
            }
            if (isBlank(bookingId)) {
                return HttpResponses.error(mapper, 400, "bookingId is required");
            }
            if (callerRole == null) callerRole = "";

            // ---- Authorization & booking load (customer OR assigned agent) ----
            BookingItem booking;
            String uploader; // "CUSTOMER" or "TRAVEL_AGENT"

            if ("TRAVEL_AGENT".equalsIgnoreCase(callerRole)) {
                if (callerEmail == null || callerEmail.isBlank()) {
                    return HttpResponses.error(mapper, 403, "missing email claim");
                }
                TravelAgent agent = agentsRepo.findByEmail(callerEmail);
                if (agent == null || !"TRAVEL_AGENT".equals(agent.getRole())) {
                    return HttpResponses.error(mapper, 403, "not a registered travel agent");
                }
                booking = bookingsRepo.getByBookingId(bookingId);
                if (booking == null) {
                    return HttpResponses.error(mapper, 404, "booking not found");
                }
                if (booking.getAgentEmail() == null || !booking.getAgentEmail().equalsIgnoreCase(callerEmail)) {
                    return HttpResponses.error(mapper, 403, "not the assigned travel agent");
                }
                uploader = "TRAVEL_AGENT";
            } else {
                // Customer can only upload to their own booking (PK = sub)
                booking = bookingsRepo.get(callerSub, bookingId);
                if (booking == null) {
                    return HttpResponses.error(mapper, 404, "booking not found");
                }
                uploader = "CUSTOMER";
            }

            // only allow uploads when BOOKED or CONFIRMED ----
            var st = booking.getStatusEnum();
            if (st == null || (st != BookingStatus.BOOKED && st != BookingStatus.CONFIRMED)) {
                return HttpResponses.error(
                        mapper,
                        409,
                        "documents can be uploaded only when the booking is BOOKED or CONFIRMED (current: " +
                                (booking.getStatus() == null ? "UNKNOWN" : booking.getStatus()) + ")"
                );
            }

            // ---- Parse body ----
            UploadDocumentsRequest body;
            try {
                body = mapper.readValue(event.getBody(), UploadDocumentsRequest.class);
            } catch (Exception e) {
                return HttpResponses.error(mapper, 400, "invalid json body");
            }
            if ((body.payments == null || body.payments.isEmpty())
                    && (body.guestDocuments == null || body.guestDocuments.isEmpty())) {
                return HttpResponses.error(mapper, 400, "no documents provided");
            }

            // ---- Guardrails ----
            final long MAX_SINGLE_SIZE_BYTES = 9_500_000L; // ~9.5MB (API Gateway 10MB limit)
            final int MAX_FILES = 40;
            int counter = 0;

            // helpers
            java.util.function.BiFunction<String, String, String> normalizeContentType = (typeOrExt, fileName) -> {
                String t = typeOrExt == null ? "" : typeOrExt.trim().toLowerCase(java.util.Locale.ROOT);
                String fn = fileName == null ? "" : fileName.trim().toLowerCase(java.util.Locale.ROOT);
                if (t.startsWith("application/") || t.startsWith("image/") || t.startsWith("text/")) return t;
                if ("pdf".equals(t) || fn.endsWith(".pdf")) return "application/pdf";
                if ("png".equals(t) || fn.endsWith(".png")) return "image/png";
                if ("jpg".equals(t) || "jpeg".equals(t) || fn.matches(".*\\.(jpg|jpeg)$")) return "image/jpeg";
                return "application/octet-stream";
            };
            java.util.function.Function<String, byte[]> decode = (b64) -> {
                String clean = stripDataPrefix(b64);
                return java.util.Base64.getDecoder().decode(clean);
            };

            final long now = System.currentTimeMillis();

            // ---- Payments ----
            if (body.payments != null) {
                for (UploadDocumentsRequest.PaymentDocument p : body.payments) {
                    if (p == null || isBlank(p.base64encodedDocument)) continue;
                    if (++counter > MAX_FILES) {
                        return HttpResponses.error(mapper, 400, "too many files");
                    }

                    final byte[] bytes;
                    try {
                        bytes = decode.apply(p.base64encodedDocument);
                    } catch (IllegalArgumentException badB64) {
                        return HttpResponses.error(mapper, 400, "invalid base64 in payments");
                    }
                    if (bytes.length > MAX_SINGLE_SIZE_BYTES) {
                        return HttpResponses.error(mapper, 413, "file too large (payments)");
                    }

                    String contentType = normalizeContentType.apply(p.type, p.fileName);
                    String s3Key = s3Docs.putBase64(
                            bookingId,
                            "payments",
                            null,
                            p.fileName,
                            contentType,
                            p.base64encodedDocument
                    );

                    // index record
                    DocumentRecord rec = new DocumentRecord();
                    rec.setBookingId(bookingId);
                    rec.setDocId(newDocId("PAYMENT", p.fileName, now));
                    rec.setS3Key(s3Key);
                    rec.setCategory("PAYMENT");
                    rec.setGuestName(null);
                    rec.setFileName(p.fileName);
                    rec.setContentType(contentType);
                    rec.setSizeBytes((long) bytes.length);
                    try { rec.setSha256(sha256Hex(bytes)); } catch (Exception ignored) {}
                    rec.setUploadedAtEpoch(now);
                    rec.setUploadedBy(uploader);

                    documentsRepo.put(rec);
                }
            }

            // ---- Guest documents ----
            if (body.guestDocuments != null) {
                for (UploadDocumentsRequest.GuestDocuments gd : body.guestDocuments) {
                    if (gd == null || gd.documents == null) continue;
                    String guestName = (gd.userName == null || gd.userName.trim().isEmpty())
                            ? "guest" : gd.userName.trim();

                    for (UploadDocumentsRequest.GuestDoc d : gd.documents) {
                        if (d == null || isBlank(d.base64encodedDocument)) continue;
                        if (++counter > MAX_FILES) {
                            return HttpResponses.error(mapper, 400, "too many files");
                        }

                        final byte[] bytes;
                        try {
                            bytes = decode.apply(d.base64encodedDocument);
                        } catch (IllegalArgumentException badB64) {
                            return HttpResponses.error(mapper, 400, "invalid base64 in guestDocuments");
                        }
                        if (bytes.length > MAX_SINGLE_SIZE_BYTES) {
                            return HttpResponses.error(mapper, 413, "file too large (guestDocuments)");
                        }

                        String contentType = normalizeContentType.apply(d.type, d.fileName);
                        String s3Key = s3Docs.putBase64(
                                bookingId,
                                "guests",
                                guestName,
                                d.fileName,
                                contentType,
                                d.base64encodedDocument
                        );

                        DocumentRecord rec = new DocumentRecord();
                        rec.setBookingId(bookingId);
                        rec.setDocId(newDocId("PASSPORT", d.fileName, now));
                        rec.setS3Key(s3Key);
                        rec.setCategory("PASSPORT");
                        rec.setGuestName(guestName);
                        rec.setFileName(d.fileName);
                        rec.setContentType(contentType);
                        rec.setSizeBytes((long) bytes.length);
                        try { rec.setSha256(sha256Hex(bytes)); } catch (Exception ignored) {}
                        rec.setUploadedAtEpoch(now);
                        rec.setUploadedBy(uploader);

                        documentsRepo.put(rec);
                    }
                }
            }

            return HttpResponses.json(mapper, 201, new UploadDocumentsResponse("Documents uploaded successfully."));

        } catch (Exception e) {
            log.error("uploadDocuments failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }



    @Override
    public APIGatewayProxyResponseEvent listDocuments(APIGatewayProxyRequestEvent event, String bookingId) {
        try {
            // ---- Auth ----
            String callerSub = extractClaim(event, "sub");
            String callerRole = extractClaim(event, "custom:role");
            String callerEmail = extractClaim(event, "email");

            if (callerSub == null) {
                return HttpResponses.error(mapper, 401, "authentication required: please log in or sign up");
            }
            if (isBlank(bookingId)) {
                return HttpResponses.error(mapper, 400, "bookingId is required");
            }
            if (callerRole == null) callerRole = "";

            // ---- Authorization & booking load (consistent with view()) ----
            BookingItem booking;

            if ("CUSTOMER".equalsIgnoreCase(callerRole)) {
                // Customer can only access their own booking (PK = sub)
                booking = bookingsRepo.get(callerSub, bookingId);
                if (booking == null) {
                    return HttpResponses.error(mapper, 404, "booking not found");
                }
            } else if ("TRAVEL_AGENT".equalsIgnoreCase(callerRole)) {
                if (callerEmail == null || callerEmail.isBlank()) {
                    return HttpResponses.error(mapper, 403, "missing email claim");
                }
                // verify the agent exists and has proper role
                TravelAgent agent = agentsRepo.findByEmail(callerEmail);
                if (agent == null || !"TRAVEL_AGENT".equals(agent.getRole())) {
                    return HttpResponses.error(mapper, 403, "not a registered travel agent");
                }
                // load by bookingId and ensure assignment
                booking = bookingsRepo.getByBookingId(bookingId);
                if (booking == null) {
                    return HttpResponses.error(mapper, 404, "booking not found");
                }
                if (booking.getAgentEmail() == null || !booking.getAgentEmail().equalsIgnoreCase(callerEmail)) {
                    return HttpResponses.error(mapper, 403, "not the assigned travel agent");
                }
            } else {
                return HttpResponses.error(mapper, 403, "incorrect role: must be either customer or travel agent");
            }

            // ---- Fetch & shape documents ----
            java.util.List<DocumentRecord> all = documentsRepo.listByBookingId(bookingId);

            // Payments (category = PAYMENT)
            java.util.List<ListDocumentsResponse.FileRef> payments = all.stream()
                    .filter(r -> "PAYMENT".equalsIgnoreCase(r.getCategory()))
                    .map(r -> new ListDocumentsResponse.FileRef(r.getDocId(), fallback(r.getFileName(), "file"), s3HttpUrl(r.getS3Key())))
                    .collect(java.util.stream.Collectors.toList());

            // Guest docs grouped by guestName (category = PASSPORT)
            java.util.Map<String, java.util.List<DocumentRecord>> byGuest = all.stream()
                    .filter(r -> "PASSPORT".equalsIgnoreCase(r.getCategory()))
                    .collect(java.util.stream.Collectors.groupingBy(r -> {
                        String g = r.getGuestName();
                        return (g == null || g.trim().isEmpty()) ? "guest" : g.trim();
                    }));

            java.util.List<ListDocumentsResponse.GuestGroup> guestGroups = new java.util.ArrayList<>();
            for (java.util.Map.Entry<String, java.util.List<DocumentRecord>> e : byGuest.entrySet()) {
                java.util.List<ListDocumentsResponse.FileRef> refs = e.getValue().stream()
                        .map(r -> new ListDocumentsResponse.FileRef(r.getDocId(), fallback(r.getFileName(), "file"), s3HttpUrl(r.getS3Key())))
                        .collect(java.util.stream.Collectors.toList());
                guestGroups.add(new ListDocumentsResponse.GuestGroup(e.getKey(), refs));
            }

            ListDocumentsResponse resp = new ListDocumentsResponse(payments, guestGroups);
            return HttpResponses.json(mapper, 200, resp);

        } catch (Exception e) {
            log.error("listDocuments failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent deleteDocument(APIGatewayProxyRequestEvent event,
                                                       String bookingId,
                                                       String documentId) {
        try {
            // ---- Auth ----
            String callerSub   = extractClaim(event, "sub");
            String callerRole  = extractClaim(event, "custom:role");
            String callerEmail = extractClaim(event, "email");

            if (callerSub == null) {
                return HttpResponses.error(mapper, 401, "authentication required: please log in or sign up");
            }
            if (isBlank(bookingId) || isBlank(documentId)) {
                return HttpResponses.error(mapper, 400, "bookingId and documentId are required");
            }
            if (callerRole == null) callerRole = "";

            // ---- Authorization & booking load (consistent with list/view) ----
            BookingItem booking;
            if ("CUSTOMER".equalsIgnoreCase(callerRole)) {
                booking = bookingsRepo.get(callerSub, bookingId);
                if (booking == null) return HttpResponses.error(mapper, 404, "booking not found");
            } else if ("TRAVEL_AGENT".equalsIgnoreCase(callerRole)) {
                if (callerEmail == null || callerEmail.isBlank()) {
                    return HttpResponses.error(mapper, 403, "missing email claim");
                }
                TravelAgent agent = agentsRepo.findByEmail(callerEmail);
                if (agent == null || !"TRAVEL_AGENT".equals(agent.getRole())) {
                    return HttpResponses.error(mapper, 403, "not a registered travel agent");
                }
                booking = bookingsRepo.getByBookingId(bookingId);
                if (booking == null) return HttpResponses.error(mapper, 404, "booking not found");
                if (booking.getAgentEmail() == null || !booking.getAgentEmail().equalsIgnoreCase(callerEmail)) {
                    return HttpResponses.error(mapper, 403, "not the assigned travel agent");
                }
            } else {
                return HttpResponses.error(mapper, 403, "incorrect role: must be either customer or travel agent");
            }

            // ---- Load the document record ----
            DocumentRecord rec = documentsRepo.get(bookingId, documentId);
            if (rec == null) {
                return HttpResponses.error(mapper, 404, "document not found");
            }

            // (Optional) Extra guard: ensure guestName belongs to this booking's passengers, etc.

            // ---- Delete in S3 then index ----
            try {
                s3Docs.deleteObject(rec.getS3Key());
            } catch (Exception s3e) {
                log.error("Failed deleting S3 object {} for booking {}", rec.getS3Key(), bookingId, s3e);
                // If you prefer strict behavior, return 502/500 here.
                // I’ll proceed only if delete succeeded; otherwise fail:
                return HttpResponses.error(mapper, 502, "failed to delete object from storage");
            }

            try {
                documentsRepo.delete(bookingId, documentId);
            } catch (Exception ddb) {
                log.error("Deleted S3 but failed to delete index {}#{}; manual cleanup needed",
                        bookingId, documentId, ddb);
                // You can still return 200 but warn; here we return 200 to keep UX simple.
            }

            return HttpResponses.json(mapper, 200, java.util.Map.of("message", "Document deleted successfully."));

        } catch (Exception e) {
            log.error("deleteDocument failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    private static String fallback(String v, String def) {
        return (v == null || v.isBlank()) ? def : v;
    }

    private static String newDocId(String category, String fileName, long nowEpochMs) {
        String uuid8 = java.util.UUID.randomUUID().toString().replace("-", "").substring(0, 8);
        String tail  = safeDocIdTail(fileName); // you already have this helper
        return nowEpochMs + "~" + uuid8 + "~" + category + "~" + tail;
    }

    private static String stripDataPrefix(String base64) {
        if (base64 == null) return "";
        int comma = base64.indexOf(',');
        if (base64.regionMatches(true, 0, "data:", 0, 5) && comma > 0) {
            return base64.substring(comma + 1);
        }
        return base64;
    }

    private static String safeDocIdTail(String fileName) {
        if (fileName == null || fileName.trim().isEmpty()) return "file";
        String base = fileName.trim();
        base = base.replace("\\", "/");
        int i = base.lastIndexOf('/');
        if (i >= 0) base = base.substring(i + 1);
        // keep compact and URL-safe-ish
        return base.replaceAll("[^A-Za-z0-9._-]+", "-");
    }

    private static String sha256Hex(byte[] bytes) throws Exception {
        java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
        byte[] dig = md.digest(bytes);
        StringBuilder sb = new StringBuilder(dig.length * 2);
        for (byte b : dig) sb.append(String.format("%02x", b));
        return sb.toString();
    }

    private static String shortHash(byte[] bytes) throws Exception {
        String full = sha256Hex(bytes);
        return full.substring(0, 8);
    }


    private static String extractClaim(APIGatewayProxyRequestEvent event, String claimName) {
        if (event == null || event.getRequestContext() == null) return null;
        var auth = event.getRequestContext().getAuthorizer();
        if (auth == null) return null;
        Object claimsObj = auth.get("claims");
        if (!(claimsObj instanceof java.util.Map)) return null;
        Object v = ((java.util.Map<?, ?>) claimsObj).get(claimName);
        return v == null ? null : v.toString();
    }

    private String s3HttpUrl(String key) {
        try {
            return new java.net.URI(
                    "https",
                    bookingDocsBucket + ".s3." + awsRegion + ".amazonaws.com",
                    "/" + (key == null ? "" : key),
                    null
            ).toASCIIString();
        } catch (Exception e) {
            return "https://" + bookingDocsBucket + ".s3." + awsRegion + ".amazonaws.com/" + (key == null ? "" : key);
        }
    }


}
