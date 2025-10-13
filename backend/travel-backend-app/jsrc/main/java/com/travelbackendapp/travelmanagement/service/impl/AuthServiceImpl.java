package com.travelbackendapp.travelmanagement.service.impl;

import com.travelbackendapp.travelmanagement.model.api.request.SignUpRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.SignInRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.request.UpdateNameRequestDTO;
import com.travelbackendapp.travelmanagement.model.api.response.*;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;
import com.travelbackendapp.travelmanagement.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.inject.Inject;
import javax.inject.Singleton;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
@Singleton
public class AuthServiceImpl implements AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthServiceImpl.class);
    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;
    private final TravelAgentRepository travelAgentRepository;
    
    @Inject
    public AuthServiceImpl(CognitoIdentityProviderClient cognitoClient, 
                           String userPoolId,
                           TravelAgentRepository travelAgentRepository) {
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
        this.travelAgentRepository = travelAgentRepository;
    }
    
    @Override
    public SignUpResponseDTO signUp(SignUpRequestDTO signUpRequest) {
        try {
            // Check if user already exists
            if (userExists(signUpRequest.getEmail())) {
                log.warn("User with email {} already exists", signUpRequest.getEmail());
                return new SignUpResponseDTO("Email already exists");
            }
            
            // Determine user role based on Travel Agent list
            String userRole = determineUserRole(signUpRequest.getEmail());
            log.info("Assigning role '{}' to user: {}", userRole, signUpRequest.getEmail());
            
            // Create user in Cognito using adminCreateUser to create a confirmed user
            AdminCreateUserRequest cognitoRequest = AdminCreateUserRequest.builder()
                .userPoolId(userPoolId)
                .username(signUpRequest.getEmail())
                .userAttributes(
                    AttributeType.builder()
                        .name("email")
                        .value(signUpRequest.getEmail())
                        .build(),
                    AttributeType.builder()
                        .name("given_name")
                        .value(signUpRequest.getFirstName())
                        .build(),
                    AttributeType.builder()
                        .name("family_name")
                        .value(signUpRequest.getLastName())
                        .build(),
                    AttributeType.builder()
                        .name("custom:role")
                        .value(userRole)
                        .build()
                )
                .messageAction(MessageActionType.SUPPRESS) // Suppress welcome email
                .temporaryPassword(signUpRequest.getPassword())
                .build();
            
            AdminCreateUserResponse cognitoResponse = cognitoClient.adminCreateUser(cognitoRequest);
            
            // Set the permanent password
            AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                .userPoolId(userPoolId)
                .username(signUpRequest.getEmail())
                .password(signUpRequest.getPassword())
                .permanent(true)
                .build();
            
            cognitoClient.adminSetUserPassword(setPasswordRequest);
            
            if (cognitoResponse.sdkHttpResponse().isSuccessful()) {
                log.info("User {} successfully registered and confirmed", signUpRequest.getEmail());
                return new SignUpResponseDTO("Account created successfully");
            } else {
                log.error("Failed to register user: {}", cognitoResponse.sdkHttpResponse().statusCode());
                return new SignUpResponseDTO("Failed to create account");
            }
            
        } catch (UsernameExistsException e) {
            log.warn("Username already exists: {}", signUpRequest.getEmail());
            return new SignUpResponseDTO("Email already exists");
        } catch (InvalidPasswordException e) {
            log.warn("Invalid password for user: {}", signUpRequest.getEmail());
            return new SignUpResponseDTO("Invalid password provided");
        } catch (InvalidParameterException e) {
            log.warn("Invalid parameters for sign up: {}", e.getMessage());
            return new SignUpResponseDTO("Invalid input provided");
        } catch (Exception e) {
            log.error("Unexpected error during sign up for user: {}", signUpRequest.getEmail(), e);
            return new SignUpResponseDTO("Internal server error");
        }
    }
    
    private boolean userExists(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }
        
        try {
            AdminGetUserRequest request = AdminGetUserRequest.builder()
                .userPoolId(userPoolId)
                .username(email.trim())
                .build();
            
            cognitoClient.adminGetUser(request);
            return true; // User exists
        } catch (UserNotFoundException e) {
            return false; // User doesn't exist
        } catch (Exception e) {
            log.warn("Error checking if user exists for email {}: {}", email, e.getMessage());
            return false; // Assume user doesn't exist on error to be safe
        }
    }
    
    private String getClientId() {
        // This should be retrieved from environment variables or configuration
        // For now, we'll need to add this to the environment variables
        return System.getenv("COGNITO_CLIENT_ID");
    }
    
    @Override
    public SignInResponseDTO signIn(SignInRequestDTO signInRequest) {
        try {
            log.info("Processing sign in request for user: {}", signInRequest.getEmail());
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", signInRequest.getEmail());
            authParameters.put("PASSWORD", signInRequest.getPassword());
            // Authenticate user with Cognito
            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                .userPoolId(userPoolId)
                .clientId(getClientId())
                .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                .authParameters(authParameters)
                .build();
            
            AdminInitiateAuthResponse authResponse = cognitoClient.adminInitiateAuth(authRequest);
            
            if (authResponse.authenticationResult() != null) {
                String idToken = authResponse.authenticationResult().idToken();
                
                // Get user details from Cognito
                AdminGetUserRequest getUserRequest = AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(signInRequest.getEmail())
                    .build();
                
                AdminGetUserResponse getUserResponse = cognitoClient.adminGetUser(getUserRequest);
                
                // Extract user information
                String firstName = getUserResponse.userAttributes().stream()
                    .filter(attr -> "given_name".equals(attr.name()))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElse("");
                
                String lastName = getUserResponse.userAttributes().stream()
                    .filter(attr -> "family_name".equals(attr.name()))
                    .findFirst()
                    .map(AttributeType::value)
                    .orElse("");
                
                String userName = firstName + " " + lastName;
                
                // Get user role from Cognito custom attributes
                String role = getUserRoleFromCognito(getUserResponse);
                
                log.info("User {} successfully signed in with role: {}", signInRequest.getEmail(), role);
                
                return new SignInResponseDTO(idToken, role, userName, signInRequest.getEmail());
            } else {
                log.warn("Authentication failed for user: {}", signInRequest.getEmail());
                return new SignInResponseDTO(null, null, null, null);
            }
            
        } catch (NotAuthorizedException e) {
            log.warn("Invalid credentials for user: {}", signInRequest.getEmail());
            return new SignInResponseDTO(null, null, null, null);
        } catch (UserNotFoundException e) {
            log.warn("User not found: {}", signInRequest.getEmail());
            return new SignInResponseDTO(null, null, null, null);
        } catch (UserNotConfirmedException e) {
            log.warn("User not confirmed: {}", signInRequest.getEmail());
            return new SignInResponseDTO(null, null, null, null);
        } catch (Exception e) {
            log.error("Unexpected error during sign in for user: {}", signInRequest.getEmail(), e);
            return new SignInResponseDTO(null, null, null, null);
        }
    }
    
    /**
     * Determine user role based on Travel Agent list
     * @param email User's email address
     * @return User role (TRAVEL_AGENT, ADMIN, or CUSTOMER)
     */
    private String determineUserRole(String email) {
        try {
            TravelAgent travelAgent = travelAgentRepository.findByEmail(email);
            if (travelAgent != null) {
                log.info("Found user {} in Travel Agent list with role: {}", email, travelAgent.getRole());
                return travelAgent.getRole();
            } else {
                log.info("User {} not found in Travel Agent list, assigning CUSTOMER role", email);
                return "CUSTOMER";
            }
        } catch (Exception e) {
            log.warn("Error checking Travel Agent list for user: {}, defaulting to CUSTOMER role", email, e);
            return "CUSTOMER";
        }
    }
    
    /**
     * Extract user role from Cognito user attributes
     * @param getUserResponse Cognito user response
     * @return User role from custom:role attribute
     */
    private String getUserRoleFromCognito(AdminGetUserResponse getUserResponse) {
        return getUserResponse.userAttributes().stream()
                .filter(attr -> "custom:role".equals(attr.name()))
                .findFirst()
                .map(AttributeType::value)
                .orElse("CUSTOMER"); // Default role if not found
    }


    @Override
    public UserInfoResponseDTO getUserInfo(String userIdEmail) {
        try {
            AdminGetUserResponse resp = cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userIdEmail)
                    .build());

            String first = resp.userAttributes().stream()
                    .filter(a -> "given_name".equals(a.name()))
                    .map(AttributeType::value).findFirst().orElse("");

            String last = resp.userAttributes().stream()
                    .filter(a -> "family_name".equals(a.name()))
                    .map(AttributeType::value).findFirst().orElse("");

            String role = resp.userAttributes().stream()
                    .filter(a -> "custom:role".equals(a.name()))
                    .map(AttributeType::value).findFirst().orElse("CUSTOMER");

            // Optional image
            String imageUrl = resp.userAttributes().stream()
                    .filter(a -> "picture".equals(a.name()))
                    .map(AttributeType::value).findFirst().orElse(null);

            return new UserInfoResponseDTO(first, last, imageUrl, role);

        } catch (UserNotFoundException e) {
            log.warn("User not found for getUserInfo: {}", userIdEmail);
            return null; // controller will turn this into 404
        } catch (Exception e) {
            log.error("Unexpected error in getUserInfo for user: {}", userIdEmail, e);
            return null;
        }
    }

    // service/impl/AuthServiceImpl.java  (replace updateUserName)
    @Override
    public UpdateNameResponseDTO updateUserName(String userIdEmail, UpdateNameRequestDTO req) {
        try {
            List<AttributeType> attrs = new ArrayList<>();
            if (req.getFirstName() != null && !req.getFirstName().trim().isEmpty()) {
                attrs.add(AttributeType.builder().name("given_name").value(req.getFirstName().trim()).build());
            }
            if (req.getLastName() != null && !req.getLastName().trim().isEmpty()) {
                attrs.add(AttributeType.builder().name("family_name").value(req.getLastName().trim()).build());
            }

            if (attrs.isEmpty()) {
                return new UpdateNameResponseDTO("firstName or lastName is required");
            }

            AdminUpdateUserAttributesResponse updateResp =
                    cognitoClient.adminUpdateUserAttributes(
                            AdminUpdateUserAttributesRequest.builder()
                                    .userPoolId(userPoolId)
                                    .username(userIdEmail)
                                    .userAttributes(attrs)
                                    .build()
                    );

            if (updateResp.sdkHttpResponse().isSuccessful()) {
                return new UpdateNameResponseDTO("Your account has been updated successfully");
            }
            log.error("Failed to update user name; status={}", updateResp.sdkHttpResponse().statusCode());
            return new UpdateNameResponseDTO("Failed to update user name");

        } catch (UserNotFoundException e) {
            log.warn("User not found for updateUserName: {}", userIdEmail);
            return new UpdateNameResponseDTO("User not found");
        } catch (InvalidParameterException e) {
            log.warn("Invalid parameters for updateUserName: {}", e.getMessage());
            return new UpdateNameResponseDTO("Invalid input provided");
        } catch (Exception e) {
            log.error("Unexpected error in updateUserName for user: {}", userIdEmail, e);
            return new UpdateNameResponseDTO("Internal server error");
        }
    }


    @Override
    public UpdateProfileResponseDTO changePassword(String userEmail, String currentPassword, String newPassword) {
        try {
            log.info("Changing password for user: {}", userEmail);

            // 1) Verify current password by authenticating with Cognito (admin flow)
            Map<String, String> authParameters = new HashMap<>();
            authParameters.put("USERNAME", userEmail);
            authParameters.put("PASSWORD", currentPassword);

            AdminInitiateAuthRequest authRequest = AdminInitiateAuthRequest.builder()
                    .userPoolId(userPoolId)
                    .clientId(getClientId())
                    .authFlow(AuthFlowType.ADMIN_NO_SRP_AUTH)
                    .authParameters(authParameters)
                    .build();

            cognitoClient.adminInitiateAuth(authRequest); // throws on bad creds

            // 2) Set new password permanently
            AdminSetUserPasswordRequest setReq = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(userEmail)
                    .password(newPassword)
                    .permanent(true)
                    .build();

            AdminSetUserPasswordResponse setRes = cognitoClient.adminSetUserPassword(setReq);

            if (setRes.sdkHttpResponse().isSuccessful()) {
                return new UpdateProfileResponseDTO("Your password has been updated successfully", true);
            }
            log.error("AdminSetUserPassword failed, status={}", setRes.sdkHttpResponse().statusCode());
            return new UpdateProfileResponseDTO("Failed to update password", false);

        } catch (NotAuthorizedException e) {
            log.warn("Current password invalid for user: {}", userEmail);
            return new UpdateProfileResponseDTO("Current password is incorrect", false);
        } catch (UserNotFoundException e) {
            log.warn("User not found during changePassword: {}", userEmail);
            return new UpdateProfileResponseDTO("User not found", false);
        } catch (InvalidPasswordException e) {
            log.warn("New password does not meet policy for user: {}", userEmail);
            return new UpdateProfileResponseDTO("New password does not meet policy requirements", false);
        } catch (Exception e) {
            log.error("Unexpected error during changePassword for user: {}", userEmail, e);
            return new UpdateProfileResponseDTO("Internal server error", false);
        }
    }

    // service/impl/AuthServiceImpl.java
    @Override
    public UpdateProfileResponseDTO updateUserPicture(String userEmail, String imageUrl) {
        try {
            AdminUpdateUserAttributesResponse updateResp =
                    cognitoClient.adminUpdateUserAttributes(AdminUpdateUserAttributesRequest.builder()
                            .userPoolId(userPoolId)
                            .username(userEmail)
                            .userAttributes(
                                    AttributeType.builder().name("picture").value(imageUrl).build()
                            )
                            .build());

            if (updateResp.sdkHttpResponse().isSuccessful()) {
                return new UpdateProfileResponseDTO("Avatar updated successfully", true);
            }
            log.error("Failed to update picture; status={}", updateResp.sdkHttpResponse().statusCode());
            return new UpdateProfileResponseDTO("Failed to update avatar", false);

        } catch (UserNotFoundException e) {
            log.warn("User not found for updateUserPicture: {}", userEmail);
            return new UpdateProfileResponseDTO("User not found", false);
        } catch (InvalidParameterException e) {
            log.warn("Invalid parameters for updateUserPicture: {}", e.getMessage());
            return new UpdateProfileResponseDTO("Invalid input provided", false);
        } catch (Exception e) {
            log.error("Unexpected error in updateUserPicture for user: {}", userEmail, e);
            return new UpdateProfileResponseDTO("Internal server error", false);
        }
    }

}
