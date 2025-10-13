package com.travelbackendapp.travelmanagement.service.impl;

import com.travelbackendapp.travelmanagement.model.api.request.SignInRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.SignUpRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.response.SignInResponseDTO;
import com.travelbackendapp.travelmanagement.model.api.response.SignUpResponseDTO;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;

import org.jetbrains.annotations.TestOnly;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidParameterException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.InvalidPasswordException;
import software.amazon.awssdk.services.cognitoidentityprovider.model.UsernameExistsException;

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
        when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
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
        when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
            .thenReturn(createResponse);
        
        when(cognitoClient.adminSetUserPassword(any(AdminSetUserPasswordRequest.class)))
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
        when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
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
        when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
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
        verify(cognitoClient).adminInitiateAuth(any(AdminInitiateAuthRequest.class));
        verify(cognitoClient).adminGetUser(any(AdminGetUserRequest.class));
    }

	@Test
	@DisplayName("Should handle NotAuthorizedException during sign in")
	void shouldHandleNotAuthorizedException() throws Exception {
		// Given
		SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);
		
		// Mock authentication failure
		when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
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
		verify(cognitoClient).adminInitiateAuth(any(AdminInitiateAuthRequest.class));
		verify(cognitoClient, never()).adminGetUser(any(AdminGetUserRequest.class));	

	}

	@Test
	@DisplayName("Should handle UserNotFoundException during sign in")
	void shouldHandleUserNotFoundException() throws Exception {
		// Given
		SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);
		
		// Mock authentication failure
		when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
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
		verify(cognitoClient).adminInitiateAuth(any(AdminInitiateAuthRequest.class));
		verify(cognitoClient, never()).adminGetUser(any(AdminGetUserRequest.class));	

	}

	@Disabled("This test is not implemented yet")
	@Test
	@DisplayName("Should handle Exception during sign in")
	void shouldHandleException() throws Exception {
		// Given
		SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);

		// Mock authentication failure
		when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
			.thenThrow(Exception.class);
		
		// When
		SignInResponseDTO result = authService.signIn(signInRequest);
		
		// Then
		assertNotNull(result);
		assertNull(result.getIdToken());
		assertNull(result.getRole());
		assertNull(result.getUserName());
		assertNull(result.getEmail());
		//TODO: This should be improved to return an error when adminInitiateAuth throws an exception
		
		// Verify Cognito calls
		verify(cognitoClient).adminInitiateAuth(any(AdminInitiateAuthRequest.class));
		verify(cognitoClient, never()).adminGetUser(any(AdminGetUserRequest.class));	
	}

	@Disabled("This test is not implemented yet")
	@Test
	@DisplayName("Should handle UserNotConfirmedException during sign in")
	void shouldHandleUserNotConfirmedException() throws Exception {
		// Given
		SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);
		
		// Mock authentication failure
		when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
			.thenThrow(UserNotConfirmedException.builder().build());
		
		// When
		SignInResponseDTO result = authService.signIn(signInRequest);
		
		// Then
		assertNotNull(result);
		assertNull(result.getIdToken());
		assertNull(result.getRole());
		assertNull(result.getUserName());
		assertNull(result.getEmail());

		//TODO: This should be improved to return an error when adminInitiateAuth throws a UserNotConfirmedException
		
		// Verify Cognito calls
		verify(cognitoClient).adminInitiateAuth(any(AdminInitiateAuthRequest.class));
		verify(cognitoClient, never()).adminGetUser(any(AdminGetUserRequest.class));	

	
	}
	// --- SignIn Error Handling Tests ---

	@Test
	@DisplayName("Should handle generic Exception during sign in")
	void shouldHandleGenericExceptionDuringSignIn() throws Exception {
		// Given
		SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);
		
		// Mock generic Exception during authentication
		when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
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
		verify(cognitoClient).adminInitiateAuth(any(AdminInitiateAuthRequest.class));
		verify(cognitoClient, never()).adminGetUser(any(AdminGetUserRequest.class));
	}
	// --- SignUp Error Handling Tests ---

	@Test
	@DisplayName("Should handle UsernameExistsException during sign up")
	void shouldHandleUsernameExistsException() throws Exception {
		// Given
		SignUpRequestDTO signUpRequest = createSignUpRequest();
		
		// Mock user doesn't exist initially
		when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
			.thenThrow(UserNotFoundException.builder().build());
		when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(null);
		
		// Mock UsernameExistsException during user creation
		when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
			.thenThrow(UsernameExistsException.builder().build());

		// When
		SignUpResponseDTO result = authService.signUp(signUpRequest);

		// Then
		assertNotNull(result);
		assertEquals("Email already exists", result.getMessage());
		
		// Verify Cognito calls
		verify(cognitoClient).adminGetUser(any(AdminGetUserRequest.class));
		verify(cognitoClient).adminCreateUser(any(AdminCreateUserRequest.class));
		verify(cognitoClient, never()).adminSetUserPassword(any(AdminSetUserPasswordRequest.class));
	}

	@Test
	@DisplayName("Should handle InvalidPasswordException during sign up")
	void shouldHandleInvalidPasswordException() throws Exception {
		// Given
		SignUpRequestDTO signUpRequest = createSignUpRequest();
		
		// Mock user doesn't exist initially
		when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
			.thenThrow(UserNotFoundException.builder().build());
		when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(null);
		
		// Mock InvalidPasswordException during user creation
		when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
			.thenThrow(InvalidPasswordException.builder().build());

		// When
		SignUpResponseDTO result = authService.signUp(signUpRequest);

		// Then
		assertNotNull(result);
		assertEquals("Invalid password provided", result.getMessage());
		
		// Verify Cognito calls
		verify(cognitoClient).adminGetUser(any(AdminGetUserRequest.class));
		verify(cognitoClient).adminCreateUser(any(AdminCreateUserRequest.class));
		verify(cognitoClient, never()).adminSetUserPassword(any(AdminSetUserPasswordRequest.class));
	}

	@Test
	@DisplayName("Should handle InvalidParameterException during sign up")
	void shouldHandleInvalidParameterException() throws Exception {
		// Given
		SignUpRequestDTO signUpRequest = createSignUpRequest();
		
		// Mock user doesn't exist initially
		when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
			.thenThrow(UserNotFoundException.builder().build());
		when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(null);
		
		// Mock InvalidParameterException during user creation
		when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
			.thenThrow(InvalidParameterException.builder().build());

		// When
		SignUpResponseDTO result = authService.signUp(signUpRequest);

		// Then
		assertNotNull(result);
		assertEquals("Invalid input provided", result.getMessage());
		
		// Verify Cognito calls
		verify(cognitoClient).adminGetUser(any(AdminGetUserRequest.class));
		verify(cognitoClient).adminCreateUser(any(AdminCreateUserRequest.class));
		verify(cognitoClient, never()).adminSetUserPassword(any(AdminSetUserPasswordRequest.class));
	}

	@Test
	@DisplayName("Should handle generic Exception during sign up")
	void shouldHandleGenericException() throws Exception {
		// Given
		SignUpRequestDTO signUpRequest = createSignUpRequest();
		
		// Mock user doesn't exist initially
		when(cognitoClient.adminGetUser(any(AdminGetUserRequest.class)))
			.thenThrow(UserNotFoundException.builder().build());
		when(travelAgentRepository.findByEmail(TEST_EMAIL)).thenReturn(null);
		
		// Mock generic Exception during user creation
		when(cognitoClient.adminCreateUser(any(AdminCreateUserRequest.class)))
			.thenThrow(new RuntimeException("Unexpected error"));

		// When
		SignUpResponseDTO result = authService.signUp(signUpRequest);

		// Then
		assertNotNull(result);
		assertEquals("Internal server error", result.getMessage());
		
		// Verify Cognito calls
		verify(cognitoClient).adminGetUser(any(AdminGetUserRequest.class));
		verify(cognitoClient).adminCreateUser(any(AdminCreateUserRequest.class));
		verify(cognitoClient, never()).adminSetUserPassword(any(AdminSetUserPasswordRequest.class));
	}

	@Disabled("This test is not implemented yet")
	@Test
	@DisplayName("Should handle user account locked during sign in")
	void shouldHandleUserAccountLockedDuringSignIn() throws Exception {
		// Given
		SignInRequestDTO signInRequest = new SignInRequestDTO(TEST_EMAIL, TEST_PASSWORD);
		
		// Mock authentication failure due to account being locked
		when(cognitoClient.adminInitiateAuth(any(AdminInitiateAuthRequest.class)))
			.thenThrow(NotAuthorizedException.builder()
				.message("User account is locked due to multiple failed attempts")
				.build());
		
		// When
		SignInResponseDTO result = authService.signIn(signInRequest);
		
		// Then
		assertNotNull(result);
		assertNull(result.getIdToken());
		assertNull(result.getRole());
		assertNull(result.getUserName());
		assertNull(result.getEmail());
		
		// TODO: This should be improved to return a specific error response
		// Expected improvement: Return error with specific message like "Account locked"
		// instead of generic null values
		
		// Verify Cognito calls
		verify(cognitoClient).adminInitiateAuth(any(AdminInitiateAuthRequest.class));
		verify(cognitoClient, never()).adminGetUser(any(AdminGetUserRequest.class));
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
