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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("RequestRouter Tests")
class RequestRouterTest {

    @Mock
    private ToursService toursService;

    @Mock
    private AuthController authController;

    @Mock
    private BookingsService bookingsService;

    @Mock
    private AiChatService aiChatService;

    @Mock
    private UsersController usersController;

    @Mock
    private TravelAgentsService travelAgentsService;

    @Mock
    private Context context;

    private RequestRouter requestRouter;

    @BeforeEach
    void setUp() {
        requestRouter = new RequestRouter(
                toursService,
                authController,
                bookingsService,
                aiChatService,
                usersController,
                travelAgentsService
        );
    }

    @Test
    @DisplayName("Should route OPTIONS request")
    void shouldRouteOptionsRequest() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("OPTIONS");

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        verifyNoInteractions(toursService, authController, bookingsService);
    }

    @Test
    @DisplayName("Should route GET /tours/available")
    void shouldRouteGetToursAvailable() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/tours/available");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(toursService.getAvailableTours(event)).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursService).getAvailableTours(event);
    }

    @Test
    @DisplayName("Should route GET /tours/destinations")
    void shouldRouteGetToursDestinations() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/tours/destinations");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(toursService.getDestinations(event)).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursService).getDestinations(event);
    }

    @Test
    @DisplayName("Should route POST /tours")
    void shouldRoutePostTours() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/tours");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(201);

        when(toursService.createTour(event)).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        verify(toursService).createTour(event);
    }

    @Test
    @DisplayName("Should route GET /tours/{id}")
    void shouldRouteGetTourById() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/tours/T-123");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(toursService.getTourDetails(event, "T-123")).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursService).getTourDetails(event, "T-123");
    }

    @Test
    @DisplayName("Should route PUT /tours/{id}")
    void shouldRoutePutTourById() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("PUT");
        event.setPath("/tours/T-123");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(toursService.updateTour(event, "T-123")).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursService).updateTour(event, "T-123");
    }

    @Test
    @DisplayName("Should route DELETE /tours/{id}")
    void shouldRouteDeleteTourById() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("DELETE");
        event.setPath("/tours/T-123");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(toursService.deleteTour(event, "T-123")).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursService).deleteTour(event, "T-123");
    }

    @Test
    @DisplayName("Should route POST /auth/sign-up")
    void shouldRoutePostSignUp() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/auth/sign-up");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(authController.signUp(event, context)).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(authController).signUp(event, context);
    }

    @Test
    @DisplayName("Should route POST /auth/sign-in")
    void shouldRoutePostSignIn() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/auth/sign-in");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(authController.signIn(event, context)).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(authController).signIn(event, context);
    }

    @Test
    @DisplayName("Should route POST /bookings")
    void shouldRoutePostBookings() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/bookings");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(201);

        when(bookingsService.create(event)).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        verify(bookingsService).create(event);
    }

    @Test
    @DisplayName("Should route GET /bookings")
    void shouldRouteGetBookings() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/bookings");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(bookingsService.view(event)).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(bookingsService).view(event);
    }

    @Test
    @DisplayName("Should route POST /ai/chat")
    void shouldRoutePostAiChat() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("POST");
        event.setPath("/ai/chat");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(aiChatService.chat(event)).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(aiChatService).chat(event);
    }

    @Test
    @DisplayName("Should route GET /users/{email}")
    void shouldRouteGetUser() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/users/user@test.com");
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(usersController.getUser(event, context, "user@test.com")).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(usersController).getUser(event, context, "user@test.com");
    }

    @Test
    @DisplayName("Should return 404 for unknown route")
    void shouldReturn404ForUnknownRoute() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/unknown/route");

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
    }

    @Test
    @DisplayName("Should handle stage prefix in path")
    void shouldHandleStagePrefixInPath() {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        event.setHttpMethod("GET");
        event.setPath("/dev/tours/available");
        event.setRequestContext(new com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext()
                .withStage("dev"));
        APIGatewayProxyResponseEvent mockResponse = new APIGatewayProxyResponseEvent().withStatusCode(200);

        when(toursService.getAvailableTours(any())).thenReturn(mockResponse);

        // When
        APIGatewayProxyResponseEvent response = requestRouter.route(event, context);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(toursService).getAvailableTours(any());
    }
}

