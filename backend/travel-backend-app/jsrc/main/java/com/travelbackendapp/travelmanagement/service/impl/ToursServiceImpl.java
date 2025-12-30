package com.travelbackendapp.travelmanagement.service.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.domain.BookingStatus;
import com.travelbackendapp.travelmanagement.domain.ReviewSort;
import com.travelbackendapp.travelmanagement.exceptions.BadRequestException;
import com.travelbackendapp.travelmanagement.mapper.ReviewMapper;
import com.travelbackendapp.travelmanagement.mapper.TourDetailsMapper;
import com.travelbackendapp.travelmanagement.mapper.TourMapper;
import com.travelbackendapp.travelmanagement.model.api.request.CreateReviewRequest;
import com.travelbackendapp.travelmanagement.model.api.request.CreateTourRequest;
import com.travelbackendapp.travelmanagement.model.api.request.DestinationsSearchRequest;
import com.travelbackendapp.travelmanagement.model.api.request.ToursSearchRequest;
import com.travelbackendapp.travelmanagement.model.api.request.UpdateTourRequest;
import com.travelbackendapp.travelmanagement.model.api.response.*;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;
import com.travelbackendapp.travelmanagement.model.entity.BookingItem;
import com.travelbackendapp.travelmanagement.model.entity.ReviewItem;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import com.travelbackendapp.travelmanagement.repository.BookingsRepository;
import com.travelbackendapp.travelmanagement.repository.ReviewsRepository;
import com.travelbackendapp.travelmanagement.repository.ToursRepository;
import com.travelbackendapp.travelmanagement.service.ToursService;
import com.travelbackendapp.travelmanagement.util.HttpResponses;
import com.travelbackendapp.travelmanagement.util.Pagination;

import javax.inject.Named;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import javax.inject.Inject;
import javax.validation.ConstraintViolation;
import java.util.*;
import java.util.stream.Collectors;

import static com.travelbackendapp.travelmanagement.util.RequestUtils.*;

public class ToursServiceImpl implements ToursService {

    private static final Logger log = LoggerFactory.getLogger(ToursServiceImpl.class);

    private final ToursRepository repo;
    private final ObjectMapper mapper;
    private final ReviewsRepository reviewsRepo;
    private final BookingsRepository bookingsRepo;
    private final Validator validator;
    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;
    private final TravelAgentRepository agentsRepo;


    @Inject
    public ToursServiceImpl(ToursRepository repo, ReviewsRepository reviewsRepo,
                            BookingsRepository bookingsRepo, ObjectMapper mapper, Validator validator,
                            CognitoIdentityProviderClient cognitoClient,
                            @Named("userPoolId") String userPoolId,
                            TravelAgentRepository agentsRepo) {
        this.repo = repo;
        this.reviewsRepo = reviewsRepo;
        this.mapper = mapper;
        this.bookingsRepo = bookingsRepo;
        this.validator = validator;
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
        this.agentsRepo = agentsRepo;
    }

    @Override
    public APIGatewayProxyResponseEvent getAvailableTours(APIGatewayProxyRequestEvent event) {
        log.info("Entering getAvailableTours");
        try {
            Map<String, String> q = event.getQueryStringParameters() == null
                    ? Collections.emptyMap()
                    : event.getQueryStringParameters();

            // Parse + validate
            ToursSearchRequest req = ToursSearchRequest.fromQuery(q, mapper);
            // In your handler getAvailableTours(...)
            log.info("Filters: dest={}, startDate={}, endDate={}, duration={}, buckets={}, mealPlans={}, tourTypes={}, adults={}, children={}, sortBy={}",
                    req.destination, req.startDate, req.endDate, req.duration, req.durationBuckets,
                    req.mealPlans, req.tourTypes, req.guests.adults, req.guests.children, req.sortBy);

            List<TourItem> items = repo.findAvailableTours(
                    req.destination,
                    req.startDate,
                    req.endDate,
                    req.duration,
                    req.mealPlans,
                    req.tourTypes,
                    req.guests.adults,
                    req.guests.children
            );


            if (!req.durationBuckets.isEmpty()) {
                items = items.stream()
                        .filter(it -> req.durationBuckets.stream().anyMatch(b -> b.matches(it.getDurations())))
                        .collect(Collectors.toList());
            }


            // Sort
            items.sort(req.sortBy.comparator());

            // Pagination
            int totalItems = items.size();
            int totalPages = Pagination.totalPages(totalItems, req.pageSize);
            int[] rr = Pagination.range(totalItems, req.page, req.pageSize);

            List<TourResponse> tours = items.subList(rr[0], rr[1]).stream()
                    .map(TourMapper::toResponse)
                    .collect(Collectors.toList());

            return HttpResponses.json(mapper, 200,
                    new ToursPageResponse(tours, req.page, req.pageSize, totalPages, totalItems));

        } catch (BadRequestException bre) {
            log.warn("Bad request: {}", bre.getMessage());
            return HttpResponses.error(mapper, 400, bre.getMessage());
        } catch (Exception e) {
            log.error("getAvailableTours failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        } finally {
            log.info("Exiting getAvailableTours");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent getDestinations(APIGatewayProxyRequestEvent event) {
        try {
            Map<String, String> q = event.getQueryStringParameters() == null
                    ? Collections.emptyMap()
                    : event.getQueryStringParameters();

            DestinationsSearchRequest req = DestinationsSearchRequest.fromQuery(q);
            var list = repo.findDestinationsLike(req.query, req.limit);

            return HttpResponses.json(mapper, 200, new DestinationsResponse(list));

        } catch (BadRequestException bre) {
            return HttpResponses.error(mapper, 400, bre.getMessage());
        } catch (Exception e) {
            log.error("getDestinations failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent getTourDetails(APIGatewayProxyRequestEvent event, String tourId) {
        try {
            if (isBlank(tourId)) {
                return HttpResponses.error(mapper, 400, "missing tour id");
            }

            TourItem item = repo.getById(tourId.trim()).orElse(null);
            if (item == null) {
                return HttpResponses.error(mapper, 404, "tour not found");
            }

            // hide non-bookable tours if needed
            if (item.getAvailablePackages() != null && item.getAvailablePackages() <= 0) {
                return HttpResponses.error(mapper, 404, "tour not available");
            }

            TourDetailResponse resp = TourDetailsMapper.toDetails(item);
            return HttpResponses.json(mapper, 200, resp);

        } catch (Exception e) {
            log.error("getTourDetails failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent getTourReviews(APIGatewayProxyRequestEvent event, String tourId) {
        if (isBlank(tourId)) {
            return HttpResponses.error(mapper, 400, "missing tour id");
        }

        Map<String, String> q = event.getQueryStringParameters() == null
                ? Collections.emptyMap()
                : event.getQueryStringParameters();

        int page = clampMin(parseIntOrDefault(q.get("page"), 1), 1);
        int pageSize = clampRange(parseIntOrDefault(q.get("pageSize"), 4), 1, 50);
        ReviewSort sort = ReviewSort.from(q.get("sortBy"));

        try {
            // 1) Ensure the tour exists
            Optional<TourItem> tourOpt = repo.getById(tourId);
            if (tourOpt.isEmpty()) {
                return HttpResponses.error(mapper, 404, "tour not found");
            }

            // 2) Load reviews for this tour (scan by attribute)
            List<ReviewItem> items = reviewsRepo.scanByTourId(tourId);

            // 3) Sort
            Comparator<ReviewItem> byDateAsc = Comparator.comparing(
                    r -> nullToMinDate(r.getCreatedAt()) // ISO yyyy-MM-dd
            );
            Comparator<ReviewItem> byRateAsc = Comparator.comparing(
                    r -> r.getRate() == null ? Integer.valueOf(Integer.MIN_VALUE) : r.getRate()
            );

            switch (sort) {
                case NEWEST:
                    items.sort(byDateAsc.reversed().thenComparing(byRateAsc.reversed()));
                    break;
                case OLDEST:
                    items.sort(byDateAsc.thenComparing(byRateAsc));
                    break;
                case RATING_DESC:
                    items.sort(byRateAsc.reversed().thenComparing(byDateAsc.reversed()));
                    break;
                case RATING_ASC:
                    items.sort(byRateAsc.thenComparing(byDateAsc));
                    break;
                default:
                    items.sort(byDateAsc.reversed().thenComparing(byRateAsc.reversed()));
            }

            // 4) Pagination
            int total = items.size();
            int totalPages = Pagination.totalPages(total, pageSize);
            if (totalPages == 0) page = 1;
            else if (page > totalPages) page = totalPages;

            int[] rr = Pagination.range(total, page, pageSize);
            List<ReviewItem> pageItems = items.subList(rr[0], rr[1]);

            // 5) Enrich authorName / authorImageUrl if missing (per-request cache by authorId/sub)
            Map<String, UserProfile> profileCache = new HashMap<>();
            List<ReviewResponse> out = pageItems.stream()
                    .map(it -> {
                        String name = it.getAuthorName();
                        String pic  = it.getAuthorImageUrl();

                        if ((name == null || name.isBlank()) || (pic == null || pic.isBlank())) {
                            String sub = it.getAuthorId();
                            if (sub != null && !sub.isBlank()) {
                                UserProfile prof = profileCache.computeIfAbsent(sub, this::fetchUserProfileBySub);
                                if (name == null || name.isBlank()) name = prof.name;
                                if (pic == null || pic.isBlank()) pic = prof.picture;
                            }
                        }

                        ReviewResponse r = new ReviewResponse();
                        r.authorName = (name != null && !name.isBlank())
                                ? name
                                : (it.getAuthorId() != null ? it.getAuthorId() : "");
                        r.authorImageUrl = (pic != null && !pic.isBlank()) ? pic : null;
                        r.createdAt = it.getCreatedAt();
                        r.rate = it.getRate() == null ? 0 : it.getRate();
                        r.reviewContent = it.getReviewContent();
                        return r;
                    })
                    .collect(Collectors.toList());

            ReviewsPageResponse resp = new ReviewsPageResponse(out, page, pageSize, totalPages, total);
            return HttpResponses.json(mapper, 200, resp);

        } catch (Exception e) {
            log.error("getTourReviews failed for tourId={}", tourId, e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }


    @Override
    public APIGatewayProxyResponseEvent postTourReview(APIGatewayProxyRequestEvent event, String tourId) {
        // Auth
        String userId = extractClaim(event, "sub"); // Cognito sub
        if (userId == null) {
            return HttpResponses.error(mapper, 401, "authentication required");
        }

        // Path validation
        if (isBlank(tourId)) {
            return HttpResponses.error(mapper, 400, "missing tour id");
        }
        if (repo.getById(tourId).isEmpty()) {
            return HttpResponses.error(mapper, 404, "tour not found");
        }

        // Parse + bean-validate JSON
        CreateReviewRequest body;
        try {
            body = mapper.readValue(event.getBody(), CreateReviewRequest.class);
        } catch (Exception e) {
            return HttpResponses.error(mapper, 400, "invalid json body");
        }
        var violations = validator.validate(body);
        if (!violations.isEmpty()) {
            String msg = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
            return HttpResponses.error(mapper, 400, msg);
        }

        // Body-level business validation (comment rule)
        if (body.rate <= 3 && (body.comment == null || body.comment.trim().isEmpty())) {
            return HttpResponses.error(mapper, 400, "comment is required for ratings 1-3");
        }

        // Load and verify the booking belongs to this caller and matches the tour
        BookingItem booking = bookingsRepo.get(userId, body.bookingId);
        if (booking == null) {
            // (Optionally fall back to getByBookingId if your UI only has bookingId;
            // but this keeps ownership strict and fast.)
            return HttpResponses.error(mapper, 404, "booking not found");
        }
        if (!tourId.equals(booking.getTourId())) {
            return HttpResponses.error(mapper, 400, "booking does not belong to this tour");
        }

        // Status gates
        BookingStatus st = booking.getStatusEnum();
        ReviewItem existingForBooking = reviewsRepo.getByBookingId(body.bookingId);

        if (existingForBooking == null) {
            // CREATE: allowed when STARTED or FINISHED
            if (!(st == BookingStatus.STARTED || st == BookingStatus.FINISHED)) {
                return HttpResponses.error(mapper, 400, "you can provide feedback once the tour has started");
            }
        } else {
            // UPDATE: allowed only when FINISHED
            if (st != BookingStatus.FINISHED) {
                return HttpResponses.error(mapper, 400, "you can update your feedback once the booking is finished");
            }
        }

        // Build/Update the review record
        ReviewItem ri = (existingForBooking != null) ? existingForBooking : new ReviewItem();
        ri.setBookingId(body.bookingId);
        ri.setTourId(tourId);
        ri.setAuthorId(userId);
        ri.setRate(body.rate);
        ri.setReviewContent(body.comment);
        String today = ReviewItem.todayIso();
        if (existingForBooking == null) ri.setCreatedAt(today);
        ri.setUpdatedAt(today);

        String given = extractClaim(event, "given_name");
        String family = extractClaim(event, "family_name");
        String displayName = null;
        if ((given != null && !given.isBlank()) || (family != null && !family.isBlank())) {
            displayName = ((given == null ? "" : given) + " " + (family == null ? "" : family)).trim();
        }
        String avatarFromClaims = extractClaim(event, "picture");

        if ((displayName == null || displayName.isBlank()) || (avatarFromClaims == null || avatarFromClaims.isBlank())) {
            UserProfile prof = fetchUserProfileBySub(userId);
            if (displayName == null || displayName.isBlank()) displayName = prof.name;
            if (avatarFromClaims == null || avatarFromClaims.isBlank()) avatarFromClaims = prof.picture;
        }

        ri.setAuthorName(displayName);      // NEW snapshot
        ri.setAuthorImageUrl(avatarFromClaims); // snapshot (may still be null if not set)


        // Aggregate impact on TourItem.rating / reviews
        if (existingForBooking == null) {
            // First time reviewing THIS booking.
            // If this user has never contributed to this tour before, increment count;
            // otherwise replace their previous contribution (count unchanged).
            Integer oldUserRateForTour = findUsersPreviousTourRate(userId, tourId, body.bookingId); // null if none
            reviewsRepo.put(ri);
            if (oldUserRateForTour == null) {
                repo.applyNewReview(tourId, body.rate);
            } else {
                repo.updateReview(tourId, oldUserRateForTour, body.rate);
            }
            return HttpResponses.json(mapper, 201, ReviewMapper.toResponse(ri));
        } else {
            // Update same booking’s review → replace contribution (count unchanged)
            int oldRate = existingForBooking.getRate();
            reviewsRepo.update(ri);
            if (oldRate != body.rate) {
                repo.updateReview(tourId, oldRate, body.rate);
            }
            return HttpResponses.json(mapper, 200, ReviewMapper.toResponse(ri));
        }
    }


    /** Find previous contribution (if any) this user already made to this tour via another booking. */
    private Integer findUsersPreviousTourRate(String userId, String tourId, String excludeBookingId) {
        for (BookingItem b : bookingsRepo.findByUserId(userId)) {
            if (tourId.equals(b.getTourId()) && !b.getBookingId().equals(excludeBookingId)) {
                ReviewItem r = reviewsRepo.getByBookingId(b.getBookingId());
                if (r != null && r.getRate() != null) return r.getRate();
            }
        }
        return null;
    }

    private static boolean isBlank(String s) { return s == null || s.trim().isEmpty(); }

    private static String extractClaim(APIGatewayProxyRequestEvent event, String claimName) {
        if (event == null || event.getRequestContext() == null) return null;
        var auth = event.getRequestContext().getAuthorizer();
        if (auth == null) return null;
        Object claimsObj = auth.get("claims");
        if (!(claimsObj instanceof java.util.Map)) return null;
        Object v = ((java.util.Map<?, ?>) claimsObj).get(claimName);
        return v == null ? null : v.toString();
    }

    // ------------ Helpers

    private static String nullToMinDate(String s) {
        return (s == null || s.isBlank()) ? "0000-01-01" : s;
    }

    private static final class UserProfile {
        final String name;
        final String picture;
        UserProfile(String name, String picture) { this.name = name; this.picture = picture; }
    }

    private UserProfile fetchUserProfileBySub(String sub) {
        try {
            // Query by sub. Username may be email in your pool, but sub is reliable & immutable.
            var resp = cognitoClient.listUsers(b -> b
                    .userPoolId(userPoolId)
                    .filter("sub = \"" + sub + "\"")
                    .limit(1)
            );
            if (resp.users().isEmpty()) return new UserProfile(null, null);

            var user = resp.users().get(0);
            String first = null, last = null, picture = null;
            for (var a : user.attributes()) {
                switch (a.name()) {
                    case "given_name": first = a.value(); break;
                    case "family_name": last = a.value(); break;
                    case "picture": picture = a.value(); break;
                }
            }
            String name = (first == null && last == null) ? null :
                    (first == null ? last : (last == null ? first : (first + " " + last))).trim();
            return new UserProfile(name, picture);
        } catch (Exception e) {
            log.warn("fetchUserProfileBySub failed for sub={}: {}", sub, e.toString());
            return new UserProfile(null, null);
        }
    }

    @Override
    public APIGatewayProxyResponseEvent createTour(APIGatewayProxyRequestEvent event) {
        try {
            // Auth: require TRAVEL_AGENT or ADMIN role
            String callerEmail = extractClaim(event, "email");
            String callerRole = extractClaim(event, "custom:role");
            
            if (callerEmail == null || callerEmail.isBlank()) {
                return HttpResponses.error(mapper, 401, "authentication required");
            }
            
            if (!"TRAVEL_AGENT".equals(callerRole) && !"ADMIN".equals(callerRole)) {
                return HttpResponses.error(mapper, 403, "only travel agents or admins can create tours");
            }
            
            // Verify agent/admin exists and has proper role
            TravelAgent agent = agentsRepo.findByEmail(callerEmail);
            if (agent == null || (!"TRAVEL_AGENT".equals(agent.getRole()) && !"ADMIN".equals(agent.getRole()))) {
                return HttpResponses.error(mapper, 403, "not a registered travel agent or admin");
            }
            
            // Parse and validate request
            CreateTourRequest body;
            try {
                body = mapper.readValue(event.getBody(), CreateTourRequest.class);
            } catch (Exception e) {
                return HttpResponses.error(mapper, 400, "invalid json body");
            }
            
            var violations = validator.validate(body);
            if (!violations.isEmpty()) {
                String msg = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                return HttpResponses.error(mapper, 400, msg);
            }
            
            // Generate tour ID
            String tourId = "T-" + System.currentTimeMillis();
            
            // Create TourItem
            TourItem tour = new TourItem();
            tour.setTourId(tourId);
            tour.setName(body.name);
            tour.setDestination(body.destination);
            tour.setStartDates(body.startDates);
            if (body.startDates != null && !body.startDates.isEmpty()) {
                tour.setStartDate(body.startDates.get(0));
            }
            tour.setDurations(body.durations);
            tour.setMealPlans(body.mealPlans);
            tour.setPriceFrom(body.priceFrom);
            tour.setPriceByDuration(body.priceByDuration);
            tour.setMealSupplementsPerDay(body.mealSupplementsPerDay);
            tour.setTourType(body.tourType);
            tour.setMaxAdults(body.maxAdults);
            tour.setMaxChildren(body.maxChildren);
            tour.setAvailablePackages(body.availablePackages);
            tour.setImageUrls(body.imageUrls);
            tour.setSummary(body.summary);
            tour.setAccommodation(body.accommodation);
            tour.setHotelName(body.hotelName);
            tour.setHotelDescription(body.hotelDescription);
            tour.setCustomDetails(body.customDetails);
            tour.setFreeCancellation(body.freeCancellation);
            tour.setFreeCancellationDaysBefore(body.freeCancellationDaysBefore);
            tour.setAgentEmail(callerEmail);
            tour.setRating(0.0);
            tour.setReviews(0);
            
            // Save tour
            repo.save(tour);
            
            log.info("Tour created: {} by agent: {}", tourId, callerEmail);
            return HttpResponses.json(mapper, 201, new CreateTourResponse(tourId, "Tour created successfully"));
            
        } catch (Exception e) {
            log.error("createTour failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent updateTour(APIGatewayProxyRequestEvent event, String tourId) {
        try {
            // Auth: require TRAVEL_AGENT or ADMIN role
            String callerEmail = extractClaim(event, "email");
            String callerRole = extractClaim(event, "custom:role");
            
            if (callerEmail == null || callerEmail.isBlank()) {
                return HttpResponses.error(mapper, 401, "authentication required");
            }
            
            if (!"TRAVEL_AGENT".equals(callerRole) && !"ADMIN".equals(callerRole)) {
                return HttpResponses.error(mapper, 403, "only travel agents or admins can update tours");
            }
            
            // Verify agent/admin exists and has proper role
            TravelAgent agent = agentsRepo.findByEmail(callerEmail);
            if (agent == null || (!"TRAVEL_AGENT".equals(agent.getRole()) && !"ADMIN".equals(agent.getRole()))) {
                return HttpResponses.error(mapper, 403, "not a registered travel agent or admin");
            }
            
            // Check tour exists
            Optional<TourItem> tourOpt = repo.getById(tourId);
            if (tourOpt.isEmpty()) {
                return HttpResponses.error(mapper, 404, "tour not found");
            }
            
            TourItem tour = tourOpt.get();
            
            // Verify agent owns this tour (ADMIN can update any tour)
            if (!"ADMIN".equals(callerRole)) {
                if (tour.getAgentEmail() == null || !tour.getAgentEmail().equalsIgnoreCase(callerEmail)) {
                    return HttpResponses.error(mapper, 403, "you can only update tours you created");
                }
            }
            
            // Parse and validate request
            UpdateTourRequest body;
            try {
                body = mapper.readValue(event.getBody(), UpdateTourRequest.class);
            } catch (Exception e) {
                return HttpResponses.error(mapper, 400, "invalid json body");
            }
            
            // Update fields (only non-null fields)
            if (body.name != null) tour.setName(body.name);
            if (body.destination != null) tour.setDestination(body.destination);
            if (body.startDates != null && !body.startDates.isEmpty()) {
                tour.setStartDates(body.startDates);
                tour.setStartDate(body.startDates.get(0));
            }
            if (body.durations != null) tour.setDurations(body.durations);
            if (body.mealPlans != null) tour.setMealPlans(body.mealPlans);
            if (body.priceFrom != null) tour.setPriceFrom(body.priceFrom);
            if (body.priceByDuration != null) tour.setPriceByDuration(body.priceByDuration);
            if (body.mealSupplementsPerDay != null) tour.setMealSupplementsPerDay(body.mealSupplementsPerDay);
            if (body.tourType != null) tour.setTourType(body.tourType);
            if (body.maxAdults != null) tour.setMaxAdults(body.maxAdults);
            if (body.maxChildren != null) tour.setMaxChildren(body.maxChildren);
            if (body.availablePackages != null) tour.setAvailablePackages(body.availablePackages);
            if (body.imageUrls != null) tour.setImageUrls(body.imageUrls);
            if (body.summary != null) tour.setSummary(body.summary);
            if (body.accommodation != null) tour.setAccommodation(body.accommodation);
            if (body.hotelName != null) tour.setHotelName(body.hotelName);
            if (body.hotelDescription != null) tour.setHotelDescription(body.hotelDescription);
            if (body.customDetails != null) tour.setCustomDetails(body.customDetails);
            if (body.freeCancellation != null) tour.setFreeCancellation(body.freeCancellation);
            if (body.freeCancellationDaysBefore != null) tour.setFreeCancellationDaysBefore(body.freeCancellationDaysBefore);
            
            // Save updated tour
            repo.update(tour);
            
            log.info("Tour updated: {} by agent: {}", tourId, callerEmail);
            return HttpResponses.json(mapper, 200, new UpdateTourResponse(tourId, "Tour updated successfully"));
            
        } catch (Exception e) {
            log.error("updateTour failed for tourId={}", tourId, e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent deleteTour(APIGatewayProxyRequestEvent event, String tourId) {
        try {
            // Auth: require TRAVEL_AGENT or ADMIN role
            String callerEmail = extractClaim(event, "email");
            String callerRole = extractClaim(event, "custom:role");
            
            if (callerEmail == null || callerEmail.isBlank()) {
                return HttpResponses.error(mapper, 401, "authentication required");
            }
            
            if (!"TRAVEL_AGENT".equals(callerRole) && !"ADMIN".equals(callerRole)) {
                return HttpResponses.error(mapper, 403, "only travel agents or admins can delete tours");
            }
            
            // Verify agent/admin exists and has proper role
            TravelAgent agent = agentsRepo.findByEmail(callerEmail);
            if (agent == null || (!"TRAVEL_AGENT".equals(agent.getRole()) && !"ADMIN".equals(agent.getRole()))) {
                return HttpResponses.error(mapper, 403, "not a registered travel agent or admin");
            }
            
            // Check tour exists
            Optional<TourItem> tourOpt = repo.getById(tourId);
            if (tourOpt.isEmpty()) {
                return HttpResponses.error(mapper, 404, "tour not found");
            }
            
            TourItem tour = tourOpt.get();
            
            // Verify agent owns this tour (ADMIN can delete any tour)
            if (!"ADMIN".equals(callerRole)) {
                if (tour.getAgentEmail() == null || !tour.getAgentEmail().equalsIgnoreCase(callerEmail)) {
                    return HttpResponses.error(mapper, 403, "you can only delete tours you created");
                }
            }
            
            // Delete tour
            repo.delete(tourId);
            
            log.info("Tour deleted: {} by agent: {}", tourId, callerEmail);
            return HttpResponses.json(mapper, 200, new DeleteTourResponse("Tour deleted successfully"));
            
        } catch (Exception e) {
            log.error("deleteTour failed for tourId={}", tourId, e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent getMyTours(APIGatewayProxyRequestEvent event) {
        try {
            // Auth: require TRAVEL_AGENT or ADMIN role
            String callerEmail = extractClaim(event, "email");
            String callerRole = extractClaim(event, "custom:role");
            
            if (callerEmail == null || callerEmail.isBlank()) {
                return HttpResponses.error(mapper, 401, "authentication required");
            }
            
            if (!"TRAVEL_AGENT".equals(callerRole) && !"ADMIN".equals(callerRole)) {
                return HttpResponses.error(mapper, 403, "only travel agents or admins can view tours");
            }
            
            // Verify agent/admin exists and has proper role
            TravelAgent agent = agentsRepo.findByEmail(callerEmail);
            if (agent == null || (!"TRAVEL_AGENT".equals(agent.getRole()) && !"ADMIN".equals(agent.getRole()))) {
                return HttpResponses.error(mapper, 403, "not a registered travel agent or admin");
            }
            
            // Get tours: ADMIN sees all tours, TRAVEL_AGENT sees only their own
            List<TourItem> tours;
            if ("ADMIN".equals(callerRole)) {
                // Admin can see all tours - get all tours from repository
                tours = repo.listAll();
            } else {
                // Travel agent sees only their own tours
                tours = repo.findByAgentEmail(callerEmail);
            }
            
            // Convert to TourResponse
            List<TourResponse> tourResponses = tours.stream()
                    .map(TourMapper::toResponse)
                    .collect(Collectors.toList());
            
            return HttpResponses.json(mapper, 200, new ToursPageResponse(tourResponses, 1, tourResponses.size(), 1, tourResponses.size()));
            
        } catch (Exception e) {
            log.error("getMyTours failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

}
