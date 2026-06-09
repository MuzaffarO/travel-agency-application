package com.travelbackendapp.travelmanagement.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.model.api.request.ChangePasswordRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.UpdateNameRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.UploadAvatarRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.response.UserInfoResponseDTO;
import com.travelbackendapp.travelmanagement.service.AuthService;
import com.travelbackendapp.travelmanagement.util.ImageBase64;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UsersController Tests")
class UsersControllerTest {

    @Mock
    private AuthService authService;

    @Mock
    private ObjectMapper objectMapper;

    @Mock
    private Validator validator;

    @Mock
    private S3Client s3Client;

    @Mock
    private S3Presigner s3Presigner;

    @Mock
    private Context context;

    private UsersController usersController;
    private static final String AVATARS_BUCKET = "test-avatars-bucket";
    private static final String AWS_REGION = "eu-west-3";
    private static final String TEST_EMAIL = "user@test.com";

    @BeforeEach
    void setUp() {
        usersController = new UsersController(
                authService,
                objectMapper,
                validator,
                s3Client,
                s3Presigner,
                AVATARS_BUCKET,
                AWS_REGION
        );
    }

    @Test
    @DisplayName("Should get user info successfully")
    void shouldGetUserInfoSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        UserInfoResponseDTO userInfo = new UserInfoResponseDTO("John", "Doe", "https://...", "CUSTOMER");

        when(authService.getUserInfo(TEST_EMAIL)).thenReturn(userInfo);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = usersController.getUser(event, context, TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(authService).getUserInfo(TEST_EMAIL);
    }

    @Test
    @DisplayName("Should reject get user info without authentication")
    void shouldRejectGetUserInfoWithoutAuth() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = usersController.getUser(event, context, TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(401, response.getStatusCode());
        verify(authService, never()).getUserInfo(any());
    }

    @Test
    @DisplayName("Should reject get user info for different user")
    void shouldRejectGetUserInfoForDifferentUser() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = usersController.getUser(event, context, "other@test.com");

        // Then
        assertNotNull(response);
        assertEquals(403, response.getStatusCode());
        verify(authService, never()).getUserInfo(any());
    }

    @Test
    @DisplayName("Should update user name successfully")
    void shouldUpdateUserNameSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        UpdateNameRequestDTO request = new UpdateNameRequestDTO();
        request.setFirstName("Jane");
        request.setLastName("Smith");
        String requestBody = "{}";

        event.setBody(requestBody);

        when(objectMapper.readValue(requestBody, UpdateNameRequestDTO.class)).thenReturn(request);
        when(validator.validate(request)).thenReturn(Collections.emptySet());
        when(authService.updateUserName(TEST_EMAIL, request)).thenReturn(
                new com.travelbackendapp.travelmanagement.model.api.response.UpdateNameResponseDTO("Your account has been updated successfully"));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = usersController.updateUserName(event, context, TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(authService).updateUserName(TEST_EMAIL, request);
    }

    @Test
    @DisplayName("Should update password successfully")
    void shouldUpdatePasswordSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        ChangePasswordRequestDTO request = new ChangePasswordRequestDTO();
        request.setCurrentPassword("OldPassword123!");
        request.setNewPassword("NewPassword123!");
        String requestBody = "{}";

        event.setBody(requestBody);

        when(objectMapper.readValue(requestBody, ChangePasswordRequestDTO.class)).thenReturn(request);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(authService.changePassword(eq(TEST_EMAIL), eq(request.getCurrentPassword()), eq(request.getNewPassword())))
                .thenReturn(new com.travelbackendapp.travelmanagement.model.api.response.UpdateProfileResponseDTO("Password updated successfully", true));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = usersController.updatePassword(event, context, TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(authService).changePassword(eq(TEST_EMAIL), eq(request.getCurrentPassword()), eq(request.getNewPassword()));
    }

    @Test
    @DisplayName("Should update user image successfully")
    void shouldUpdateUserImageSuccessfully() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        UploadAvatarRequestDTO request = new UploadAvatarRequestDTO();
        request.setImageBase64("iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mNk+M9QDwADhgGAWjR9awAAAABJRU5ErkJggg==");
        String requestBody = "{}";

        event.setBody(requestBody);

        UserInfoResponseDTO existingUser = new UserInfoResponseDTO("John", "Doe", null, "CUSTOMER");
        String imageUrl = "https://test-bucket.s3.eu-west-3.amazonaws.com/users/user@test.com/avatar/20250101120000.png";

        when(objectMapper.readValue(requestBody, UploadAvatarRequestDTO.class)).thenReturn(request);
        when(validator.validate(request)).thenReturn(Collections.emptySet());
        when(authService.getUserInfo(TEST_EMAIL)).thenReturn(existingUser);
        when(s3Client.putObject(any(PutObjectRequest.class), any(RequestBody.class)))
                .thenReturn(software.amazon.awssdk.services.s3.model.PutObjectResponse.builder().build());
        when(authService.updateUserPicture(eq(TEST_EMAIL), anyString()))
                .thenReturn(new com.travelbackendapp.travelmanagement.model.api.response.UpdateProfileResponseDTO("Avatar updated successfully", true));
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = usersController.updateUserImage(event, context, TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        verify(s3Client).putObject(any(PutObjectRequest.class), any(RequestBody.class));
        verify(authService).updateUserPicture(eq(TEST_EMAIL), anyString());
    }

    @Test
    @DisplayName("Should handle validation errors in updateUserName")
    void shouldHandleValidationErrorsInUpdateUserName() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        UpdateNameRequestDTO request = new UpdateNameRequestDTO();
        String requestBody = "{}";

        event.setBody(requestBody);

        when(objectMapper.readValue(requestBody, UpdateNameRequestDTO.class)).thenReturn(request);
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = usersController.updateUserName(event, context, TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        verify(authService, never()).updateUserName(any(), any());
    }

    @Test
    @DisplayName("Should handle invalid image format")
    void shouldHandleInvalidImageFormat() throws Exception {
        // Given
        APIGatewayProxyRequestEvent event = createAuthenticatedEvent();
        UploadAvatarRequestDTO request = new UploadAvatarRequestDTO();
        request.setImageBase64("invalid-base64");
        String requestBody = "{}";

        event.setBody(requestBody);

        when(objectMapper.readValue(requestBody, UploadAvatarRequestDTO.class)).thenReturn(request);
        when(validator.validate(any())).thenReturn(Collections.emptySet());
        when(objectMapper.writeValueAsString(any())).thenReturn("{}");

        // When
        APIGatewayProxyResponseEvent response = usersController.updateUserImage(event, context, TEST_EMAIL);

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        verify(s3Client, never()).putObject(any(PutObjectRequest.class), any(RequestBody.class));
    }

    // Helper methods
    private APIGatewayProxyRequestEvent createAuthenticatedEvent() {
        APIGatewayProxyRequestEvent event = new APIGatewayProxyRequestEvent();
        Map<String, Object> authorizer = new HashMap<>();
        Map<String, String> claims = new HashMap<>();
        claims.put("email", TEST_EMAIL);
        authorizer.put("claims", claims);
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext context =
                new com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext();
        context.setAuthorizer(authorizer);
        event.setRequestContext(context);
        return event;
    }
}

