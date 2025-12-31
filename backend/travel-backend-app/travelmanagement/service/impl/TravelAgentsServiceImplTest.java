package com.travelbackendapp.travelmanagement.service.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.model.api.request.CreateTravelAgentRequest;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import org.mockito.ArgumentMatchers;

@ExtendWith(MockitoExtension.class)
@DisplayName("TravelAgentsServiceImpl Tests")
class TravelAgentsServiceImplTest {

    @Mock
    private TravelAgentRepository travelAgentRepository;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    private TravelAgentsServiceImpl travelAgentsService;
    private static final String USER_POOL_ID = "test-pool-id";
    private static final String ADMIN_EMAIL = "admin@test.com";
    private static final String AGENT_EMAIL = "agent@test.com";

    @BeforeEach
    void setUp() {
        travelAgentsService = new TravelAgentsServiceImpl(
                travelAgentRepository,
                objectMapper,
                validator,
                cognitoClient,
                USER_POOL_ID
        );
    }

    @Test
    @DisplayName("Should create travel agent successfully")
    void shouldCreateTravelAgentSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(ADMIN_EMAIL, "ADMIN");
        CreateTravelAgentRequest request = createValidTravelAgentRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        when(objectMapper.readValue(requestBody, CreateTravelAgentRequest.class)).thenReturn(request);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(travelAgentRepository.findByEmail(AGENT_EMAIL)).thenReturn(null);

        AdminCreateUserResponse createResponse = AdminCreateUserResponse.builder()
                .user(software.amazon.awssdk.services.cognitoidentityprovider.model.UserType.builder()
                        .username(AGENT_EMAIL)
                        .build())
                .build();
        when(cognitoClient.adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any())).thenReturn(createResponse);
        when(cognitoClient.adminSetUserPassword(any(AdminSetUserPasswordRequest.class)))
                .thenReturn(AdminSetUserPasswordResponse.builder().build());
        doNothing().when(travelAgentRepository).save(any(TravelAgent.class));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.createTravelAgent(event);

        // Then
        assertNotNull(response);
        assertEquals(201, response.getStatusCode());
        verify(cognitoClient).adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any());
        verify(cognitoClient).adminSetUserPassword(ArgumentMatchers.<AdminSetUserPasswordRequest>any());
        verify(travelAgentRepository).save(any(TravelAgent.class));
    }

    @Test
    @DisplayName("Should reject travel agent creation without ADMIN role")
    void shouldRejectTravelAgentCreationWithoutAdminRole() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent("user@test.com", "CUSTOMER");
        event.setBody("{}");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.createTravelAgent(event);

        // Then
        assertNotNull(response);
        assertEquals(403, response.getStatusCode());
        verify(travelAgentRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should reject travel agent creation when agent already exists")
    void shouldRejectTravelAgentCreationWhenExists() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(ADMIN_EMAIL, "ADMIN");
        CreateTravelAgentRequest request = createValidTravelAgentRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        TravelAgent existingAgent = createTravelAgent();
        when(objectMapper.readValue(requestBody, CreateTravelAgentRequest.class)).thenReturn(request);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(travelAgentRepository.findByEmail(AGENT_EMAIL)).thenReturn(existingAgent);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.createTravelAgent(event);

        // Then
        assertNotNull(response);
        assertEquals(409, response.getStatusCode());
        verify(cognitoClient, never()).adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any());
    }

    @Test
    @DisplayName("Should handle UsernameExistsException")
    void shouldHandleUsernameExistsException() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(ADMIN_EMAIL, "ADMIN");
        CreateTravelAgentRequest request = createValidTravelAgentRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        when(objectMapper.readValue(requestBody, CreateTravelAgentRequest.class)).thenReturn(request);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(travelAgentRepository.findByEmail(AGENT_EMAIL)).thenReturn(null);
        when(cognitoClient.adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any()))
                .thenThrow(UsernameExistsException.builder().build());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.createTravelAgent(event);

        // Then
        assertNotNull(response);
        assertEquals(409, response.getStatusCode());
    }

    @Test
    @DisplayName("Should list travel agents successfully")
    void shouldListTravelAgentsSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(ADMIN_EMAIL, "ADMIN");
        TravelAgent agent1 = createTravelAgent();
        TravelAgent agent2 = createTravelAgent();
        agent2.setEmail("agent2@test.com");

        when(travelAgentRepository.findAll()).thenReturn(Arrays.asList(agent1, agent2));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.listTravelAgents(event);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(travelAgentRepository).findAll();
    }

    @Test
    @DisplayName("Should reject list travel agents without ADMIN role")
    void shouldRejectListTravelAgentsWithoutAdminRole() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent("user@test.com", "CUSTOMER");
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.listTravelAgents(event);

        // Then
        assertNotNull(response);
        assertEquals(403, response.getStatusCode());
        verify(travelAgentRepository, never()).findAll();
    }

    @Test
    @DisplayName("Should delete travel agent successfully")
    void shouldDeleteTravelAgentSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(ADMIN_EMAIL, "ADMIN");
        TravelAgent existingAgent = createTravelAgent();

        when(travelAgentRepository.findByEmail(AGENT_EMAIL)).thenReturn(existingAgent);
        when(cognitoClient.adminDeleteUser(ArgumentMatchers.<AdminDeleteUserRequest>any()))
                .thenReturn(AdminDeleteUserResponse.builder().build());
        doNothing().when(travelAgentRepository).deleteByEmail(AGENT_EMAIL);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.deleteTravelAgent(event, AGENT_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(cognitoClient).adminDeleteUser(ArgumentMatchers.<AdminDeleteUserRequest>any());
        verify(travelAgentRepository).deleteByEmail(AGENT_EMAIL);
    }

    @Test
    @DisplayName("Should handle UserNotFoundException when deleting")
    void shouldHandleUserNotFoundExceptionWhenDeleting() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(ADMIN_EMAIL, "ADMIN");
        TravelAgent existingAgent = createTravelAgent();

        when(travelAgentRepository.findByEmail(AGENT_EMAIL)).thenReturn(existingAgent);
        when(cognitoClient.adminDeleteUser(ArgumentMatchers.<AdminDeleteUserRequest>any()))
                .thenThrow(UserNotFoundException.builder().build());
        doNothing().when(travelAgentRepository).deleteByEmail(AGENT_EMAIL);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.deleteTravelAgent(event, AGENT_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(travelAgentRepository).deleteByEmail(AGENT_EMAIL);
    }

    @Test
    @DisplayName("Should return 404 when deleting non-existent agent")
    void shouldReturn404WhenDeletingNonExistentAgent() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(ADMIN_EMAIL, "ADMIN");

        when(travelAgentRepository.findByEmail(AGENT_EMAIL)).thenReturn(null);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.deleteTravelAgent(event, AGENT_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(404, response.getStatusCode());
        verify(cognitoClient, never()).adminDeleteUser(ArgumentMatchers.<AdminDeleteUserRequest>any());
    }

    @Test
    @DisplayName("Should handle validation errors")
    void shouldHandleValidationErrors() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent(ADMIN_EMAIL, "ADMIN");
        CreateTravelAgentRequest request = createValidTravelAgentRequest();
        String requestBody = "{}";

        event.setBody(requestBody);

        @SuppressWarnings({"unchecked", "rawtypes"})
        ConstraintViolation<CreateTravelAgentRequest> violation = mock(ConstraintViolation.class);
        when(violation.getMessage()).thenReturn("Email is required");

        when(objectMapper.readValue(requestBody, CreateTravelAgentRequest.class)).thenReturn(request);
        Set<ConstraintViolation<CreateTravelAgentRequest>> violations = Collections.singleton(violation);
        when(validator.validate(any())).thenReturn((Set) violations);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = travelAgentsService.createTravelAgent(event);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        verify(travelAgentRepository, never()).save(any());
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

    private CreateTravelAgentRequest createValidTravelAgentRequest() {
        CreateTravelAgentRequest request = new CreateTravelAgentRequest();
        request.email = AGENT_EMAIL;
        request.firstName = "Agent";
        request.lastName = "Test";
        request.role = "TRAVEL_AGENT";
        request.password = "SecurePassword123!";
        request.phone = "+1234567890";
        request.messenger = "agent@test.com";
        return request;
    }

    private TravelAgent createTravelAgent() {
        TravelAgent agent = new TravelAgent();
        agent.setEmail(AGENT_EMAIL);
        agent.setFirstName("Agent");
        agent.setLastName("Test");
        agent.setRole("TRAVEL_AGENT");
        agent.setCreatedAt("2025-01-01T00:00:00Z");
        agent.setCreatedBy(ADMIN_EMAIL);
        return agent;
    }
}

