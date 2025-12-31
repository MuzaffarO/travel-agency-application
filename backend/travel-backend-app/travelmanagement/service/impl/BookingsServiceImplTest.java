package com.travelbackendapp.travelmanagement.service.impl;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.model.api.request.CreateBookingRequest;
import com.travelbackendapp.travelmanagement.model.entity.BookingItem;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.BookingsRepository;
import com.travelbackendapp.travelmanagement.repository.BookingsStatusRepository;
import com.travelbackendapp.travelmanagement.repository.DocumentsRepository;
import com.travelbackendapp.travelmanagement.repository.ToursRepository;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;
import com.travelbackendapp.travelmanagement.service.BookingEventPublisher;
import com.travelbackendapp.travelmanagement.util.S3DocumentsStorage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.inject.Named;
import java.time.LocalDate;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("BookingsServiceImpl Tests")
class BookingsServiceImplTest {

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private ToursRepository toursRepository;

    @Mock
    private BookingsRepository bookingsRepository;

    @Mock
    private TravelAgentRepository travelAgentRepository;

    @Mock
    private BookingsStatusRepository bookingsStatusRepository;

    @Mock
    private BookingEventPublisher eventPublisher;

    @Mock
    private S3DocumentsStorage s3DocumentsStorage;

    @Mock
    private DocumentsRepository documentsRepository;

    private BookingsServiceImpl bookingsService;
    private static final String BOOKING_DOCS_BUCKET = "test-bucket";
    private static final String AWS_REGION = "eu-west-3";
    private static final String TEST_USER_ID = "user-sub-123";
    private static final String TEST_EMAIL = "user@test.com";
    private static final String TEST_TOUR_ID = "T-1234567890";

    @BeforeEach
    void setUp() {
        bookingsService = new BookingsServiceImpl(
                objectMapper,
                toursRepository,
                bookingsRepository,
                travelAgentRepository,
                bookingsStatusRepository,
                eventPublisher,
                s3DocumentsStorage,
                documentsRepository,
                BOOKING_DOCS_BUCKET,
                AWS_REGION
        );
    }

    @Test
    @DisplayName("Should create booking successfully")
    void shouldCreateBookingSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        CreateBookingRequest request = createValidBookingRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        TourItem tour = createTestTour();
        TravelAgent agent = createTravelAgent();

        when(objectMapper.readValue(requestBody, CreateBookingRequest.class)).thenReturn(request);
        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.of(tour));
        when(travelAgentRepository.findByEmail(anyString())).thenReturn(agent);
        doNothing().when(bookingsRepository).save(any(BookingItem.class));
        doNothing().when(eventPublisher).publishBookingCreated(anyString());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = bookingsService.create(event);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode() == 201 || response.getStatusCode() == 200);
        verify(bookingsRepository).save(any(BookingItem.class));
    }

    @Test
    @DisplayName("Should reject booking creation without authentication")
    void shouldRejectBookingCreationWithoutAuth() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody("{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = bookingsService.create(event);

        // Then
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        verify(bookingsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject booking for non-existent tour")
    void shouldRejectBookingForNonExistentTour() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        CreateBookingRequest request = createValidBookingRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        when(objectMapper.readValue(requestBody, CreateBookingRequest.class)).thenReturn(request);
        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = bookingsService.create(event);

        // Then
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        verify(bookingsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject booking for fully booked tour")
    void shouldRejectBookingForFullyBookedTour() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        CreateBookingRequest request = createValidBookingRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        TourItem tour = createTestTour();
        tour.setAvailablePackages(0);

        when(objectMapper.readValue(requestBody, CreateBookingRequest.class)).thenReturn(request);
        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.of(tour));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = bookingsService.create(event);

        // Then
        assertNotNull(response);
        assertEquals(409, response.getStatusCode());
        verify(bookingsRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should view bookings for CUSTOMER")
    void shouldViewBookingsForCustomer() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        BookingItem booking = createTestBooking();

        when(bookingsRepository.findByUserId(TEST_USER_ID)).thenReturn(Collections.singletonList(booking));
        when(toursRepository.getById(anyString())).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = bookingsService.view(event);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(bookingsRepository).findByUserId(TEST_USER_ID);
    }

    @Test
    @DisplayName("Should view bookings for TRAVEL_AGENT")
    void shouldViewBookingsForTravelAgent() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(TEST_EMAIL, "TRAVEL_AGENT");
        BookingItem booking = createTestBooking();
        TravelAgent agent = createTravelAgent();

        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(agent);
        when(bookingsRepository.findByAgentEmail(TEST_EMAIL)).thenReturn(Collections.singletonList(booking));
        when(toursRepository.getById(anyString())).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = bookingsService.view(event);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(bookingsRepository).findByAgentEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should cancel booking successfully")
    void shouldCancelBookingSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        BookingItem booking = createTestBooking();
        String bookingId = booking.getBookingId();

        when(bookingsRepository.getById(bookingId)).thenReturn(Optional.of(booking));
        doNothing().when(bookingsRepository).delete(bookingId);
        doNothing().when(eventPublisher).publishBookingCancelled(bookingId);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = bookingsService.cancel(event, bookingId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(bookingsRepository).delete(bookingId);
    }

    @Test
    @DisplayName("Should confirm booking successfully")
    void shouldConfirmBookingSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(TEST_EMAIL, "TRAVEL_AGENT");
        BookingItem booking = createTestBooking();
        String bookingId = booking.getBookingId();
        TravelAgent agent = createTravelAgent();

        when(bookingsRepository.getById(bookingId)).thenReturn(Optional.of(booking));
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(agent);
        doNothing().when(bookingsRepository).update(any(BookingItem.class));
        doNothing().when(eventPublisher).publishBookingConfirmed(bookingId);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = bookingsService.confirm(event, bookingId);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(bookingsRepository).update(any(BookingItem.class));
    }

    // Helper methods
    private APIGatewayProxyRequestEvent createAuthenticatedEvent() {
        return createAuthenticatedEvent(TEST_EMAIL, "CUSTOMER");
    }

    private APIGatewayProxyRequestEvent createAuthenticatedEvent(String email, String role) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, Object> authorizer = new HashMap<>();
        Map<String, String> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("sub", TEST_USER_ID);
        claims.put("custom:role", role);
        authorizer.put("claims", claims);
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext context =
                new com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext();
        context.setAuthorizer(authorizer);
        event.setRequestContext(context);
        return event;
    }

    private CreateBookingRequest createValidBookingRequest() {
        CreateBookingRequest request = new CreateBookingRequest();
        request.tourId = TEST_TOUR_ID;
        request.date = LocalDate.now().plusDays(30).toString();
        request.duration = "7 days";
        request.mealPlan = "BB";
        request.guests = new CreateBookingRequest.Guests();
        request.guests.adult = 2;
        request.guests.children = 0;
        request.personalDetails = new ArrayList<>();
        CreateBookingRequest.Person person = new CreateBookingRequest.Person();
        person.firstName = "John";
        person.lastName = "Doe";
        request.personalDetails.add(person);
        return request;
    }

    private TourItem createTestTour() {
        TourItem tour = new TourItem();
        tour.setTourId(TEST_TOUR_ID);
        tour.setName("Test Tour");
        tour.setDestination("Paris");
        tour.setStartDates(Arrays.asList(LocalDate.now().plusDays(30).toString()));
        tour.setDurations(Arrays.asList("7 days"));
        tour.setMealPlans(Arrays.asList("BB", "HB"));
        tour.setPriceFrom(1200.0);
        tour.setPriceByDuration(Map.of("7 days", 1200.0));
        tour.setMealSupplementsPerDay(Map.of("BB", 0.0, "HB", 25.0));
        tour.setMaxAdults(2);
        tour.setMaxChildren(1);
        tour.setAvailablePackages(10);
        tour.setAgentEmail("agent@test.com");
        return tour;
    }

    private TravelAgent createTravelAgent() {
        TravelAgent agent = new TravelAgent();
        agent.setEmail("agent@test.com");
        agent.setFirstName("Agent");
        agent.setLastName("Test");
        agent.setRole("TRAVEL_AGENT");
        return agent;
    }

    private BookingItem createTestBooking() {
        BookingItem booking = new BookingItem();
        booking.setBookingId("B-1234567890");
        booking.setTourId(TEST_TOUR_ID);
        booking.setUserId(TEST_USER_ID);
        booking.setStartDate(LocalDate.now().plusDays(30).toString());
        booking.setDuration("7 days");
        booking.setMealPlan("BB");
        booking.setAdults(2);
        booking.setChildren(0);
        booking.setTotalPrice(2400.0);
        booking.setAgentEmail("agent@test.com");
        return booking;
    }
}

