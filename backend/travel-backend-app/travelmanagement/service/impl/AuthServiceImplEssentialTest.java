package com.travelbackendapp.travelmanagement.service.impl;

import com.travelbackendapp.travelmanagement.model.api.request.SignInRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.SignUpRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.response.SignInResponseDTO;
import com.travelbackendapp.travelmanagement.model.api.response.SignUpResponseDTO;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import org.mockito.ArgumentMatchers;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Essential unit tests for AuthServiceImpl focusing on core business logic.
 * Tests cover role assignment and authentication with minimal AWS SDK complexity.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("AuthServiceImpl Essential Tests")
class AuthServiceImplEssentialTest {

    @Mock
    private CognitoIdentityProviderClient cognitoClient;

    @Mock
    private TravelAgentRepository travelAgentRepository;

    private AuthServiceImpl authService;

    private static final String USER_POOL_ID = "test-user-pool-id";
    private static final String CLIENT_ID = "test-client-id";
    private static final String TEST_EMAIL = "test@example.com";
    private static final String TEST_PASSWORD = "Password123!";
    private static final String TEST_FIRST_NAME = "John";
    private static final String TEST_LAST_NAME = "Doe";

    @BeforeEach
    void setUp() {
        // Set environment variable for client ID
        System.setProperty("COGNITO_CLIENT_ID", CLIENT_ID);

        authService = new AuthServiceImpl(cognitoClient, USER_POOL_ID, travelAgentRepository);
    }

    @Test
    @DisplayName("Should assign TRAVEL_AGENT role when email exists in travel agents list")
    void shouldAssignTravelAgentRole() throws Exception {
        // Given
        SignUpRequestDTO signUpRequest = createSignUpRequest();
        TravelAgent travelAgent = createTravelAgent("TRAVEL_AGENT");

        // Mock user doesn't exist in Cognito
        when(cognitoClient.adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any()))
            .thenThrow(UserNotFoundException.builder().build());

        // Mock travel agent found in repository
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(travelAgent);

        // Mock successful Cognito operations
        AdminCreateUserResponse createResponse = mock(AdminCreateUserResponse.class);
        when(createResponse.sdkHttpResponse()).thenReturn(
            software.amazon.awssdk.http.SdkHttpResponse.builder()
                .statusCode(200)
                .build()
        );
        when(cognitoClient.adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any()))
            .thenReturn(createResponse);

        when(cognitoClient.adminSetUserPassword(ArgumentMatchers.<AdminSetUserPasswordRequest>any()))
            .thenReturn(AdminSetUserPasswordResponse.builder().build());

        // When
        SignUpResponseDTO result = authService.signUp(signUpRequest);

        // Then
        assertNotNull(result);
        assertEquals("Account created successfully", result.getMessage());

        // Verify role assignment by checking the request
        verify(cognitoClient).adminCreateUser(argThat((AdminCreateUserRequest request) ->
            request.userAttributes().stream()
                .anyMatch(attr -> "custom:role".equals(attr.name()) && "TRAVEL_AGENT".equals(attr.value()))
        ));

        // Verify repository lookup
        verify(travelAgentRepository).findByEmail(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should successfully sign in and return user details with CUSTOMER role")
    void shouldSignInSuccessfullyWithCustomerRole() throws Exception {
        // Given
        SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);

        // Mock successful authentication
        AuthenticationResultType authResult = AuthenticationResultType.builder()
            .idToken("test-id-token")
            .build();
        AdminInitiateAuthResponse authResponse = AdminInitiateAuthResponse.builder()
            .authenticationResult(authResult)
            .build();
        when(cognitoClient.adminInitiateAuth(ArgumentMatchers.<AdminInitiateAuthRequest>any()))
            .thenReturn(authResponse);

        // Mock user details retrieval
        List<AttributeType> userAttributes = Arrays.asList(
            AttributeType.builder().name("given_name").value(TEST_FIRST_NAME).build(),
            AttributeType.builder().name("family_name").value(TEST_LAST_NAME).build(),
            AttributeType.builder().name("custom:role").value("CUSTOMER").build()
        );
        AdminGetUserResponse getUserResponse = AdminGetUserResponse.builder()
            .username(TEST_EMAIL)
            .userAttributes(userAttributes)
            .build();
        when(cognitoClient.adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any()))
            .thenReturn(getUserResponse);

        // When
        SignInResponseDTO result = authService.signIn(signInRequest);

        // Then
        assertNotNull(result);
        assertEquals("test-id-token", result.getIdToken());
        assertEquals("CUSTOMER", result.getRole());
        assertEquals("John Doe", result.getUserName());
        assertEquals(TEST_EMAIL, result.getEmail());

        // Verify Cognito calls
        verify(cognitoClient).adminInitiateAuth(ArgumentMatchers.<AdminInitiateAuthRequest>any());
        verify(cognitoClient).adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any());
    }

    @Test
    @DisplayName("Should handle NotAuthorizedException during sign in")
    void shouldHandleNotAuthorizedException() throws Exception {
        // Given
        SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);

        // Mock authentication failure
        when(cognitoClient.adminInitiateAuth(ArgumentMatchers.<AdminInitiateAuthRequest>any()))
            .thenThrow(NotAuthorizedException.builder().build());

        // When
        SignInResponseDTO result = authService.signIn(signInRequest);

        // Then
        assertNotNull(result);
        assertNull(result.getIdToken());
        assertNull(result.getRole());
        assertNull(result.getUserName());
        assertNull(result.getEmail());

        // Verify Cognito calls
        verify(cognitoClient).adminInitiateAuth(ArgumentMatchers.<AdminInitiateAuthRequest>any());
        verify(cognitoClient, never()).adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any());
    }

    @Test
    @DisplayName("Should handle UserNotFoundException during sign in")
    void shouldHandleUserNotFoundException() throws Exception {
        // Given
        SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);

        // Mock authentication failure
        when(cognitoClient.adminInitiateAuth(ArgumentMatchers.<AdminInitiateAuthRequest>any()))
            .thenThrow(UserNotFoundException.builder().build());

        // When
        SignInResponseDTO result = authService.signIn(signInRequest);

        // Then
        assertNotNull(result);
        assertNull(result.getIdToken());
        assertNull(result.getRole());
        assertNull(result.getUserName());
        assertNull(result.getEmail());

        // Verify Cognito calls
        verify(cognitoClient).adminInitiateAuth(ArgumentMatchers.<AdminInitiateAuthRequest>any());
        verify(cognitoClient, never()).adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any());
    }

    @Test
    @DisplayName("Should handle generic Exception during sign in")
    void shouldHandleGenericExceptionDuringSignIn() throws Exception {
        // Given
        SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);

        // Mock generic Exception during authentication
        when(cognitoClient.adminInitiateAuth(ArgumentMatchers.<AdminInitiateAuthRequest>any()))
            .thenThrow(new RuntimeException("Network error"));

        // When
        SignInResponseDTO result = authService.signIn(signInRequest);

        // Then
        assertNotNull(result);
        assertNull(result.getIdToken());
        assertNull(result.getRole());
        assertNull(result.getUserName());
        assertNull(result.getEmail());

        // Verify Cognito calls
        verify(cognitoClient).adminInitiateAuth(ArgumentMatchers.<AdminInitiateAuthRequest>any());
        verify(cognitoClient, never()).adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any());
    }

    @Test
    @DisplayName("Should handle UsernameExistsException during sign up")
    void shouldHandleUsernameExistsException() throws Exception {
        // Given
        SignUpRequestDTO signUpRequest = createSignUpRequest();

        // Mock user doesn't exist initially
        when(cognitoClient.adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any()))
            .thenThrow(UserNotFoundException.builder().build());
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(null);

        // Mock UsernameExistsException during user creation
        when(cognitoClient.adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any()))
            .thenThrow(UsernameExistsException.builder().build());

        // When
        SignUpResponseDTO result = authService.signUp(signUpRequest);

        // Then
        assertNotNull(result);
        assertEquals("Email already exists", result.getMessage());

        // Verify Cognito calls
        verify(cognitoClient).adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any());
        verify(cognitoClient).adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any());
        verify(cognitoClient, never()).adminSetUserPassword(ArgumentMatchers.<AdminSetUserPasswordRequest>any());
    }

    @Test
    @DisplayName("Should handle InvalidPasswordException during sign up")
    void shouldHandleInvalidPasswordException() throws Exception {
        // Given
        SignUpRequestDTO signUpRequest = createSignUpRequest();

        // Mock user doesn't exist initially
        when(cognitoClient.adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any()))
            .thenThrow(UserNotFoundException.builder().build());
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(null);

        // Mock InvalidPasswordException during user creation
        when(cognitoClient.adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any()))
            .thenThrow(InvalidPasswordException.builder().build());

        // When
        SignUpResponseDTO result = authService.signUp(signUpRequest);

        // Then
        assertNotNull(result);
        assertEquals("Invalid password provided", result.getMessage());

        // Verify Cognito calls
        verify(cognitoClient).adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any());
        verify(cognitoClient).adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any());
        verify(cognitoClient, never()).adminSetUserPassword(ArgumentMatchers.<AdminSetUserPasswordRequest>any());
    }

    @Test
    @DisplayName("Should handle InvalidParameterException during sign up")
    void shouldHandleInvalidParameterException() throws Exception {
        // Given
        SignUpRequestDTO signUpRequest = createSignUpRequest();

        // Mock user doesn't exist initially
        when(cognitoClient.adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any()))
            .thenThrow(UserNotFoundException.builder().build());
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(null);

        // Mock InvalidParameterException during user creation
        when(cognitoClient.adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any()))
            .thenThrow(InvalidParameterException.builder().build());

        // When
        SignUpResponseDTO result = authService.signUp(signUpRequest);

        // Then
        assertNotNull(result);
        assertEquals("Invalid input provided", result.getMessage());

        // Verify Cognito calls
        verify(cognitoClient).adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any());
        verify(cognitoClient).adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any());
        verify(cognitoClient, never()).adminSetUserPassword(ArgumentMatchers.<AdminSetUserPasswordRequest>any());
    }

    @Test
    @DisplayName("Should handle generic Exception during sign up")
    void shouldHandleGenericException() throws Exception {
        // Given
        SignUpRequestDTO signUpRequest = createSignUpRequest();

        // Mock user doesn't exist initially
        when(cognitoClient.adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any()))
            .thenThrow(UserNotFoundException.builder().build());
        when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(null);

        // Mock generic Exception during user creation
        when(cognitoClient.adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any()))
            .thenThrow(new RuntimeException("Unexpected error"));

        // When
        SignUpResponseDTO result = authService.signUp(signUpRequest);

        // Then
        assertNotNull(result);
        assertEquals("Internal server error", result.getMessage());

        // Verify Cognito calls
        verify(cognitoClient).adminGetUser(ArgumentMatchers.<AdminGetUserRequest>any());
        verify(cognitoClient).adminCreateUser(ArgumentMatchers.<AdminCreateUserRequest>any());
        verify(cognitoClient, never()).adminSetUserPassword(ArgumentMatchers.<AdminSetUserPasswordRequest>any());
    }

    // Helper methods
    private SignUpRequestDTO createSignUpRequest() {
        return new SignUpRequestDTO(TEST_FIRST_NAME, TEST_LAST_NAME, TEST_EMAIL, TEST_PASSWORD);
    }

    private TravelAgent createTravelAgent(String role) {
        TravelAgent travelAgent = new TravelAgent();
        travelAgent.setEmail(TEST_EMAIL);
        travelAgent.setFirstName(TEST_FIRST_NAME);
        travelAgent.setLastName(TEST_LAST_NAME);
        travelAgent.setRole(role);
        travelAgent.setCreatedAt("2024-01-01T00:00:00Z");
        travelAgent.setCreatedBy("admin@company.com");
        return travelAgent;
    }
}
