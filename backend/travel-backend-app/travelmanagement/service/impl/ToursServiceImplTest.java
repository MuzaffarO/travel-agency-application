package com.travelbackendapp.travelmanagement.service.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.model.api.request.CreateTourRequest;
import com.travelbackendapp.travelmanagement.model.api.request.UpdateTourRequest;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.BookingsRepository;
import com.travelbackendapp.travelmanagement.repository.ReviewsRepository;
import com.travelbackendapp.travelmanagement.repository.ToursRepository;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ToursServiceImpl Tests")
class ToursServiceImplTest {

    @Mock
    private ToursRepository toursRepository;

    @Mock
    private ReviewsRepository reviewsRepository;

    @Mock
    private BookingsRepository bookingsRepository;

    @Mock
    private TravelAgentRepository travelAgentRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    private ToursServiceImpl toursService;
    private static final String USER_POOL_ID = "test-pool-id";
    private static final String TEST_EMAIL = "agent@test.com";
    private static final String TEST_TOUR_ID = "T-1234567890";

    @BeforeEach
    void setUp() {
        toursService = new ToursServiceImpl(
                toursRepository,
                reviewsRepository,
                bookingsRepository,
                objectMapper,
                validator,
                cognitoClient,
                USER_POOL_ID,
                travelAgentRepository
        );
    }

    @Test
    @DisplayName("Should get available tours successfully")
    void shouldGetAvailableToursSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setQueryStringParameters(Map.of("page", "0", "size", "10"));

        TourItem tour = createTestTour();
        when(toursRepository.findAvailableTours(any(), any(), any(), any(), any(), any(), anyInt(), anyInt()))
                .thenReturn(Collections.singletonList(tour));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.getAvailableTours(event);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursRepository).findAvailableTours(any(), any(), any(), any(), any(), any(), anyInt(), anyInt());
    }

    @Test
    @DisplayName("Should handle bad request in getAvailableTours")
    void shouldHandleBadRequestInGetAvailableTours() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setQueryStringParameters(Map.of("startDate", "invalid-date"));

        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.getAvailableTours(event);

        // Then
        assertNotNull(response);
        assertTrue(response.getStatusCode() == 400 || response.getStatusCode() == 500);
    }

    @Test
    @DisplayName("Should get destinations successfully")
    void shouldGetDestinationsSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setQueryStringParameters(Collections.emptyMap());

        when(toursRepository.findDestinationsLike(anyString(), anyInt()))
                .thenReturn(Arrays.asList("Paris", "London", "Rome"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.getDestinations(event);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursRepository).findDestinationsLike(anyString(), anyInt());
    }

    @Test
    @DisplayName("Should get tour details successfully")
    void shouldGetTourDetailsSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        TourItem tour = createTestTour();
        tour.setAvailablePackages(10);

        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.of(tour));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.getTourDetails(event, TEST_TOUR_ID);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursRepository).getById(TEST_TOUR_ID);
    }

    @Test
    @DisplayName("Should return 404 when tour not found")
    void shouldReturn404WhenTourNotFound() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.empty());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.getTourDetails(event, TEST_TOUR_ID);

        // Then
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
    }

    @Test
    @DisplayName("Should create tour successfully as TRAVEL_AGENT")
    void shouldCreateTourSuccessfullyAsTravelAgent() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(TEST_EMAIL, "TRAVEL_AGENT");
        CreateTourRequest request = createValidTourRequest();
        String requestBody = "{\"name\":\"Test Tour\"}";

        event.setBody(requestBody);

        TravelAgent agent = createTravelAgent("TRAVEL_AGENT");
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(agent);
        when(objectMapper.readValue(requestBody, CreateTourRequest.class)).thenReturn(request);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        doNothing().when(toursRepository).save(any(TourItem.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.createTour(event);

        // Then
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        verify(toursRepository).save(any(TourItem.class));
    }

    @Test
    @DisplayName("Should reject tour creation without authentication")
    void shouldRejectTourCreationWithoutAuth() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setBody("{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.createTour(event);

        // Then
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        verify(toursRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject tour creation for CUSTOMER role")
    void shouldRejectTourCreationForCustomer() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(TEST_EMAIL, "CUSTOMER");
        event.setBody("{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.createTour(event);

        // Then
        assertNotNull(response);
        assertEquals(403, response.getStatusCode());
        verify(toursRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should update tour successfully by owner")
    void shouldUpdateTourSuccessfullyByOwner() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(TEST_EMAIL, "TRAVEL_AGENT");
        UpdateTourRequest request = createValidUpdateTourRequest();
        String requestBody = "{\"name\":\"Updated Tour\"}";

        event.setBody(requestBody);

        TourItem existingTour = createTestTour();
        existingTour.setAgentEmail(TEST_EMAIL);
        TravelAgent agent = createTravelAgent("TRAVEL_AGENT");

        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.of(existingTour));
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(agent);
        when(objectMapper.readValue(requestBody, UpdateTourRequest.class)).thenReturn(request);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        doNothing().when(toursRepository).update(any(TourItem.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.updateTour(event, TEST_TOUR_ID);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursRepository).update(any(TourItem.class));
    }

    @Test
    @DisplayName("Should reject tour update by non-owner")
    void shouldRejectTourUpdateByNonOwner() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent("other@test.com", "TRAVEL_AGENT");
        TourItem existingTour = createTestTour();
        existingTour.setAgentEmail(TEST_EMAIL);

        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.of(existingTour));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.updateTour(event, TEST_TOUR_ID);

        // Then
        assertNotNull(response);
        assertEquals(403, response.getStatusCode());
        verify(toursRepository, never()).update(any());
    }

    @Test
    @DisplayName("Should allow ADMIN to update any tour")
    void shouldAllowAdminToUpdateAnyTour() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent("admin@test.com", "ADMIN");
        UpdateTourRequest request = createValidUpdateTourRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        TourItem existingTour = createTestTour();
        existingTour.setAgentEmail(TEST_EMAIL);
        TravelAgent admin = createTravelAgent("ADMIN");

        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.of(existingTour));
        when(travelAgentRepository.findByEmail("admin@test.com")).thenReturn(admin);
        when(objectMapper.readValue(requestBody, UpdateTourRequest.class)).thenReturn(request);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        doNothing().when(toursRepository).update(any(TourItem.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.updateTour(event, TEST_TOUR_ID);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursRepository).update(any(TourItem.class));
    }

    @Test
    @DisplayName("Should delete tour successfully by owner")
    void shouldDeleteTourSuccessfullyByOwner() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(TEST_EMAIL, "TRAVEL_AGENT");
        TourItem existingTour = createTestTour();
        existingTour.setAgentEmail(TEST_EMAIL);
        TravelAgent agent = createTravelAgent("TRAVEL_AGENT");

        when(toursRepository.getById(TEST_TOUR_ID)).thenReturn(Optional.of(existingTour));
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(agent);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.deleteTour(event, TEST_TOUR_ID);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursRepository).delete(TEST_TOUR_ID);
    }

    @Test
    @DisplayName("Should get my tours successfully")
    void shouldGetMyToursSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(TEST_EMAIL, "TRAVEL_AGENT");
        TourItem tour = createTestTour();
        tour.setAgentEmail(TEST_EMAIL);
        TravelAgent agent = createTravelAgent("TRAVEL_AGENT");

        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(agent);
        when(toursRepository.findByAgentEmail(TEST_EMAIL)).thenReturn(Collections.singletonList(tour));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.getMyTours(event);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursRepository).findByAgentEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should handle validation errors in createTour")
    void shouldHandleValidationErrorsInCreateTour() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(TEST_EMAIL, "TRAVEL_AGENT");
        CreateTourRequest request = createValidTourRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        TravelAgent agent = createTravelAgent("TRAVEL_AGENT");
        @SuppressWarnings({"unchecked", "rawtypes"})
        ConstraintViolation<CreateTourRequest> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Name is required");

        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(agent);
        when(objectMapper.readValue(requestBody, CreateTourRequest.class)).thenReturn(request);
        Set<ConstraintViolation<CreateTourRequest>> violations = Collections.singleton(violation);
        when(validator.validate(any())).thenReturn((Set) violations);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = toursService.createTour(event);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        verify(toursRepository, never()).save(any());
    }

    // Helper methods
    private APIGatewayProxyRequestEvent createAuthenticatedEvent(String email, String role) {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, Object> authorizer = new HashMap<>();
        Map<String, String> claims = new HashMap<>();
        claims.put("email", email);
        claims.put("custom:role", role);
        authorizer.put("claims", claims);
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext context =
                new com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext();
        context.setAuthorizer(authorizer);
        event.setRequestContext(context);
        return event;
    }

    private TourItem createTestTour() {
        TourItem tour = new TourItem();
        tour.setTourId(TEST_TOUR_ID);
        tour.setName("Test Tour");
        tour.setDestination("Paris");
        tour.setStartDates(Arrays.asList("2025-06-01"));
        tour.setDurations(Arrays.asList("7 days"));
        tour.setMealPlans(Arrays.asList("BB", "HB"));
        tour.setPriceFrom(1200.0);
        tour.setPriceByDuration(Map.of("7 days", 1200.0));
        tour.setMealSupplementsPerDay(Map.of("BB", 0.0, "HB", 25.0));
        tour.setMaxAdults(2);
        tour.setMaxChildren(1);
        tour.setAvailablePackages(10);
        tour.setAgentEmail(TEST_EMAIL);
        tour.setRating(4.5);
        tour.setReviews(10);
        return tour;
    }

    private CreateTourRequest createValidTourRequest() {
        CreateTourRequest request = new CreateTourRequest();
        request.name = "Test Tour";
        request.destination = "Paris";
        request.startDates = Arrays.asList("2025-06-01");
        request.durations = Arrays.asList("7 days");
        request.mealPlans = Arrays.asList("BB", "HB");
        request.priceFrom = 1200.0;
        request.priceByDuration = Map.of("7 days", 1200.0);
        request.mealSupplementsPerDay = Map.of("BB", 0.0, "HB", 25.0);
        request.maxAdults = 2;
        request.maxChildren = 1;
        request.availablePackages = 10;
        return request;
    }

    private UpdateTourRequest createValidUpdateTourRequest() {
        UpdateTourRequest request = new UpdateTourRequest();
        request.name = "Updated Tour";
        request.destination = "London";
        request.startDates = Arrays.asList("2025-07-01");
        request.durations = Arrays.asList("7 days");
        request.mealPlans = Arrays.asList("BB");
        request.priceFrom = 1500.0;
        request.priceByDuration = Map.of("7 days", 1500.0);
        request.mealSupplementsPerDay = Map.of("BB", 0.0);
        request.maxAdults = 2;
        request.maxChildren = 1;
        request.availablePackages = 5;
        return request;
    }

    private TravelAgent createTravelAgent(String role) {
        TravelAgent agent = new TravelAgent();
        agent.setEmail(TEST_EMAIL);
        agent.setFirstName("Agent");
        agent.setLastName("Test");
        agent.setRole(role);
        return agent;
    }
}

