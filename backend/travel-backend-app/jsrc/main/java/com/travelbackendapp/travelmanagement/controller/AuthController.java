package com.travelbackendapp.travelmanagement.controller;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.model.api.request.SignUpRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.SignInRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.response.SignUpResponseDTO;
import com.travelbackendapp.travelmanagement.model.api.response.SignInResponseDTO;
import com.travelbackendapp.travelmanagement.model.api.response.UpdateProfileResponseDTO;
import com.travelbackendapp.travelmanagement.service.AuthService;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Singleton
public class AuthController {
    
    private static final Logger log = LoggerFactory.getLogger(AuthController.class);
    private final AuthService authService;
    private final ObjectMapper objectMapper;
    private final Validator validator;
    
    @Inject
    public AuthController(AuthService authService, ObjectMapper objectMapper, Validator validator) {
        this.authService = authService;
        this.objectMapper = objectMapper;
        this.validator = validator;
    }
    
    public APIGatewayProxyResponseEvent signUp(APIGatewayProxyRequestEvent event, Context context) {
        try {
            log.info("Processing sign up request");
            
            // Parse request body
            String requestBody = event.getBody();
            if (requestBody == null || requestBody.trim().isEmpty()) {
                return createErrorResponse(400, "Request body is required");
            }
            
            SignUpRequestDTO signUpRequest = objectMapper.readValue(requestBody, SignUpRequestDTO.class);
            
            // Validate using Bean Validation
            Set<ConstraintViolation<SignUpRequestDTO>> violations = validator.validate(signUpRequest);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                log.warn("Validation failed for sign up request: {}", errorMessage);
                return createErrorResponse(400, errorMessage);
            }
            
            // Call auth service
            SignUpResponseDTO response = authService.signUp(signUpRequest);
            
            // Determine response status based on message content
            int statusCode = determineStatusCode(response.getMessage());
            
            return createResponse(statusCode, response);
            
        } catch (Exception e) {
            log.error("Error processing sign up request", e);
            return createErrorResponse(500, "Internal server error");
        }
    }

    
    /**
     * Extract user email from API Gateway Cognito authorizer context
     * When using Cognito User Pools with API Gateway, the token is automatically validated
     * and user claims are provided in the request context
     * @param event API Gateway request event
     * @return user email or null if not found/invalid
     */
    private String extractUserEmailFromToken(APIGatewayProxyRequestEvent event) {
        try {
            // Check if request context and authorizer exist
            if (event.getRequestContext() == null || event.getRequestContext().getAuthorizer() == null) {
                log.warn("No authorizer context found in request");
                return null;
            }
            
            // Get claims from Cognito authorizer
            Map<String, Object> authorizer = event.getRequestContext().getAuthorizer();
            Map<String, Object> claims = (Map<String, Object>) authorizer.get("claims");
            
            if (claims == null) {
                log.warn("No claims found in authorizer context");
                return null;
            }
            
            // Extract email from Cognito claims
            String email = (String) claims.get("email");
            if (email == null || email.trim().isEmpty()) {
                log.warn("No email found in Cognito claims");
                return null;
            }
            
            log.info("Successfully extracted user email from Cognito claims: {}", email);
            return email.trim();
            
        } catch (Exception e) {
            log.error("Error extracting user email from Cognito authorizer context", e);
            return null;
        }
    }
    
    private int determineStatusCode(String message) {
        if ("Account created successfully".equals(message)) {
            return 201;
        } else if ("Email already exists".equals(message)) {
            return 409;
        } else {
            return 400;
        }
    }
    
    private APIGatewayProxyResponseEvent createResponse(int statusCode, SignUpResponseDTO response) {
        try {
            String responseBody = objectMapper.writeValueAsString(response);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(responseBody);
                
        } catch (Exception e) {
            log.error("Error creating response", e);
            return createErrorResponse(500, "Internal server error");
        }
    }
    
    public APIGatewayProxyResponseEvent signIn(APIGatewayProxyRequestEvent event, Context context) {
        try {
            log.info("Processing sign in request");
            
            // Parse request body
            String requestBody = event.getBody();
            if (requestBody == null || requestBody.trim().isEmpty()) {
                return createSignInErrorResponse(400, "Request body is required");
            }
            
            SignInRequestDTO signInRequest = objectMapper.readValue(requestBody, SignInRequestDTO.class);
            
            // Validate using Bean Validation
            Set<ConstraintViolation<SignInRequestDTO>> violations = validator.validate(signInRequest);
            if (!violations.isEmpty()) {
                String errorMessage = violations.stream()
                    .map(ConstraintViolation::getMessage)
                    .collect(Collectors.joining(", "));
                log.warn("Validation failed for sign in request: {}", errorMessage);
                return createSignInErrorResponse(400, errorMessage);
            }
            
            // Call auth service
            SignInResponseDTO response = authService.signIn(signInRequest);
            
            // Check if authentication was successful
            if (response.getIdToken() != null) {
                return createSignInResponse(200, response);
            } else {
                return createSignInErrorResponse(400, "Wrong password or email");
            }
            
        } catch (Exception e) {
            log.error("Error processing sign in request", e);
            return createSignInErrorResponse(500, "Internal server error");
        }
    }
    
    private APIGatewayProxyResponseEvent createSignInResponse(int statusCode, SignInResponseDTO response) {
        try {
            String responseBody = objectMapper.writeValueAsString(response);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(responseBody);
                
        } catch (Exception e) {
            log.error("Error creating sign in response", e);
            return createSignInErrorResponse(500, "Internal server error");
        }
    }
    
    private APIGatewayProxyResponseEvent createSignInErrorResponse(int statusCode, String message) {
        try {
            Map<String, String> errorResponse = new HashMap<>();
            errorResponse.put("message", message);
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(responseBody);
                
        } catch (Exception e) {
            log.error("Error creating sign in error response", e);
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"message\":\"Internal server error\"}");
        }
    }
    
    private APIGatewayProxyResponseEvent createErrorResponse(int statusCode, String message) {
        try {
            SignUpResponseDTO errorResponse = new SignUpResponseDTO(message);
            String responseBody = objectMapper.writeValueAsString(errorResponse);
            
            Map<String, String> headers = new HashMap<>();
            headers.put("Content-Type", "application/json");
            headers.put("Access-Control-Allow-Origin", "*");
            headers.put("Access-Control-Allow-Methods", "POST, OPTIONS");
            headers.put("Access-Control-Allow-Headers", "Content-Type, Authorization");
            
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(statusCode)
                .withHeaders(headers)
                .withBody(responseBody);
                
        } catch (Exception e) {
            log.error("Error creating error response", e);
            return new APIGatewayProxyResponseEvent()
                .withStatusCode(500)
                .withBody("{\"message\":\"Internal server error\"}");
        }
    }
}
