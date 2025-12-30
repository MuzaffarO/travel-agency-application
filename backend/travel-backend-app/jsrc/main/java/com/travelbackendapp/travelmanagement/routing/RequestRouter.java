package com.travelbackendapp.travelmanagement.routing;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.travelbackendapp.travelmanagement.controller.AuthController;
import com.travelbackendapp.travelmanagement.controller.UsersController;
import com.travelbackendapp.travelmanagement.service.AiChatService;
import com.travelbackendapp.travelmanagement.service.BookingsService;
import com.travelbackendapp.travelmanagement.service.ToursService;
import com.travelbackendapp.travelmanagement.service.TravelAgentsService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RequestRouter {
    private static final Logger log = LoggerFactory.getLogger(RequestRouter.class);
    private final ToursService toursService;
    private final AuthController authController;
    private final BookingsService bookingsService;
    private final AiChatService aiChatService;
    private final UsersController usersController;
    private final TravelAgentsService travelAgentsService;
    private static final Pattern TOUR_DETAILS = Pattern.compile("^/tours/([^/]+)$");
    private static final Pattern TOUR_REVIEWS = Pattern.compile("^/tours/([^/]+)/feedbacks$");
    private static final Pattern BOOKING_ID = Pattern.compile("^/bookings/([^/]+)$");
    private static final Pattern BOOKING_CONFIRM = Pattern.compile("^/bookings/([^/]+)/confirm$");
    private static final Pattern BOOKING_DOCUMENTS = Pattern.compile("^/bookings/([^/]+)/documents$");
    private static final Pattern USER_GET = Pattern.compile("^/users/([^/]+)$");
    private static final Pattern USER_NAME = Pattern.compile("^/users/([^/]+)/name$");
    private static final Pattern USER_PASSWORD = Pattern.compile("^/users/([^/]+)/password$");
    private static final Pattern USER_IMAGE = Pattern.compile("^/users/([^/]+)/image$");
    private static final Pattern TRAVEL_AGENT_EMAIL = Pattern.compile("^/admin/travel-agents/([^/]+)$");



    @Inject
    public RequestRouter(ToursService toursService, AuthController authController, BookingsService bookingsService, AiChatService aiChatService, UsersController usersController, TravelAgentsService travelAgentsService) {
        this.toursService = toursService;
        this.authController = authController;
        this.bookingsService = bookingsService;
        this.aiChatService = aiChatService;
        this.usersController = usersController;
        this.travelAgentsService = travelAgentsService;
    }

    public APIGatewayProxyResponseEvent route(APIGatewayProxyRequestEvent event, Context ctx) {
        String method = event.getHttpMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return com.travelbackendapp.travelmanagement.util.HttpResponses.empty(204);
        }


        String path = event.getPath();
        String stage = event.getRequestContext() != null ? event.getRequestContext().getStage() : null;
        String httpMethod = event.getHttpMethod();

        if (stage != null && path != null && path.startsWith("/" + stage)) {
            path = path.substring(stage.length() + 1);
        }
        log.info("Routing path={}", path);

        if ("/tours/available".equals(path)) return toursService.getAvailableTours(event);
        if ("/tours/destinations".equals(path)) return toursService.getDestinations(event);
        if ("/tours/my".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
            return toursService.getMyTours(event);
        }
        if ("/tours".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
            return toursService.createTour(event);
        }
        if ("/bookings".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
            return bookingsService.create(event);
        }
        if ("/bookings".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
            return bookingsService.view(event);
        }
        Matcher m = TOUR_DETAILS.matcher(path);
        if (m.matches()) {
            if ("PUT".equalsIgnoreCase(httpMethod)) return toursService.updateTour(event, m.group(1));
            if ("DELETE".equalsIgnoreCase(httpMethod)) return toursService.deleteTour(event, m.group(1));
            return toursService.getTourDetails(event, m.group(1));
        }

        Matcher mr = TOUR_REVIEWS.matcher(path);
        if (mr.matches()) {
            String id = mr.group(1);
            if ("GET".equalsIgnoreCase(httpMethod)) return toursService.getTourReviews(event, id);
            if ("POST".equalsIgnoreCase(httpMethod)) return toursService.postTourReview(event, id);
        }

        Matcher mb = BOOKING_ID.matcher(path);
        if (mb.matches()) {
            String id = mb.group(1);
            if ("PATCH".equalsIgnoreCase(httpMethod)) return bookingsService.update(event, id);
            if ("DELETE".equalsIgnoreCase(httpMethod)) return bookingsService.cancel(event, id);
        }

        Matcher mc = BOOKING_CONFIRM.matcher(path);
        if (mc.matches()) {
            String id = mc.group(1);
            if ("POST".equalsIgnoreCase(httpMethod)) return bookingsService.confirm(event, id);
        }

        Matcher md = BOOKING_DOCUMENTS.matcher(path);
        if (md.matches()) {
            String id = md.group(1);
            if ("POST".equalsIgnoreCase(httpMethod)) return bookingsService.uploadDocuments(event, id);
        }

        if (path != null && path.matches("^/bookings/[^/]+/documents$")) {
            String[] parts = path.split("/");
            String bookingId = parts[2]; // "/bookings/{id}/documents"
            if ("GET".equalsIgnoreCase(event.getHttpMethod())) {
                return bookingsService.listDocuments(event, bookingId);
            }
        }

        if ("DELETE".equalsIgnoreCase(httpMethod) && path != null && path.matches("^/bookings/[^/]+/documents/.+$")) {
            String[] parts = path.split("/");
            String bookingId = parts[2]; // /bookings/{id}/documents/{documentId}
            String documentId = parts[4];
            return bookingsService.deleteDocument(event, bookingId, documentId);
        }


        if ("/auth/sign-up".equals(path) && "POST".equals(event.getHttpMethod())) {
            return authController.signUp(event, ctx);
        }
        if ("/auth/sign-in".equals(path) && "POST".equals(event.getHttpMethod())) {
            return authController.signIn(event, ctx);
        }

        if ("/ai/chat".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
            return aiChatService.chat(event);
        }

        Matcher ug = USER_GET.matcher(path);
        if (ug.matches() && "GET".equalsIgnoreCase(httpMethod)) {
            return usersController.getUser(event, ctx, ug.group(1));
        }

        Matcher un = USER_NAME.matcher(path);
        if (un.matches() && "PUT".equalsIgnoreCase(httpMethod)) {
            return usersController.updateUserName(event, ctx, un.group(1));
        }

        Matcher up = USER_PASSWORD.matcher(path);
        if (up.matches() && "PUT".equalsIgnoreCase(httpMethod)) {
            String id = java.net.URLDecoder.decode(up.group(1), java.nio.charset.StandardCharsets.UTF_8);
            return usersController.updatePassword(event, ctx, id);
        }

        Matcher ui = USER_IMAGE.matcher(path);
        if (ui.matches() && "PUT".equalsIgnoreCase(httpMethod)) {
            String id = ui.group(1);
            return usersController.updateUserImage(event, ctx, id);
        }

        // Admin travel agent management routes
        if ("/admin/travel-agents".equals(path) && "POST".equalsIgnoreCase(httpMethod)) {
            return travelAgentsService.createTravelAgent(event);
        }
        if ("/admin/travel-agents".equals(path) && "GET".equalsIgnoreCase(httpMethod)) {
            return travelAgentsService.listTravelAgents(event);
        }
        Matcher ta = TRAVEL_AGENT_EMAIL.matcher(path);
        if (ta.matches() && "DELETE".equalsIgnoreCase(httpMethod)) {
            String email = java.net.URLDecoder.decode(ta.group(1), java.nio.charset.StandardCharsets.UTF_8);
            return travelAgentsService.deleteTravelAgent(event, email);
        }

        log.warn("No route matched path={}", path);
        return com.travelbackendapp.travelmanagement.util.HttpResponses.error(
                new com.fasterxml.jackson.databind.ObjectMapper(), 404, "Not Found");

    }
}
