package com.travelbackendapp.travelmanagement.service.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.model.api.request.CreateTravelAgentRequest;
import com.travelbackendapp.travelmanagement.model.api.response.*;
import com.travelbackendapp.travelmanagement.model.entity.TravelAgent;
import com.travelbackendapp.travelmanagement.repository.TravelAgentRepository;
import com.travelbackendapp.travelmanagement.service.TravelAgentsService;
import com.travelbackendapp.travelmanagement.util.HttpResponses;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.cognitoidentityprovider.CognitoIdentityProviderClient;
import software.amazon.awssdk.services.cognitoidentityprovider.model.*;

import javax.inject.Inject;
import javax.inject.Named;
import javax.validation.ConstraintViolation;
import javax.validation.Validator;
import java.time.Instant;
import java.util.List;
import java.util.stream.Collectors;

import static com.travelbackendapp.travelmanagement.util.RequestUtils.isBlank;

public class TravelAgentsServiceImpl implements TravelAgentsService {

    private static final Logger log = LoggerFactory.getLogger(TravelAgentsServiceImpl.class);

    private final TravelAgentRepository agentsRepo;
    private final ObjectMapper mapper;
    private final Validator validator;
    private final CognitoIdentityProviderClient cognitoClient;
    private final String userPoolId;

    @Inject
    public TravelAgentsServiceImpl(TravelAgentRepository agentsRepo, ObjectMapper mapper,
                                   Validator validator, CognitoIdentityProviderClient cognitoClient,
                                   @Named("userPoolId") String userPoolId) {
        this.agentsRepo = agentsRepo;
        this.mapper = mapper;
        this.validator = validator;
        this.cognitoClient = cognitoClient;
        this.userPoolId = userPoolId;
    }

    @Override
    public APIGatewayProxyResponseEvent createTravelAgent(APIGatewayProxyRequestEvent event) {
        try {
            // Auth: require ADMIN role
            String callerEmail = extractClaim(event, "email");
            String callerRole = extractClaim(event, "custom:role");

            if (callerEmail == null || callerEmail.isBlank()) {
                return HttpResponses.error(mapper, 401, "authentication required");
            }

            if (!"ADMIN".equals(callerRole)) {
                return HttpResponses.error(mapper, 403, "only admins can create travel agents");
            }

            // Verify admin exists
            TravelAgent admin = agentsRepo.findByEmail(callerEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return HttpResponses.error(mapper, 403, "not a registered admin");
            }

            // Parse and validate request
            CreateTravelAgentRequest body;
            try {
                body = mapper.readValue(event.getBody(), CreateTravelAgentRequest.class);
            } catch (Exception e) {
                return HttpResponses.error(mapper, 400, "invalid json body");
            }

            var violations = validator.validate(body);
            if (!violations.isEmpty()) {
                String msg = violations.stream().map(ConstraintViolation::getMessage).collect(Collectors.joining(", "));
                return HttpResponses.error(mapper, 400, msg);
            }

            // Check if agent already exists
            if (agentsRepo.findByEmail(body.email) != null) {
                return HttpResponses.error(mapper, 409, "travel agent with this email already exists");
            }

            // Check if Cognito user already exists
            try {
                cognitoClient.adminGetUser(AdminGetUserRequest.builder()
                        .userPoolId(userPoolId)
                        .username(body.email)
                        .build());
                return HttpResponses.error(mapper, 409, "user with this email already exists in Cognito");
            } catch (UserNotFoundException e) {
                // User doesn't exist, proceed with creation
            }

            // Create user in Cognito
            AdminCreateUserRequest cognitoRequest = AdminCreateUserRequest.builder()
                    .userPoolId(userPoolId)
                    .username(body.email)
                    .userAttributes(
                            AttributeType.builder().name("email").value(body.email).build(),
                            AttributeType.builder().name("given_name").value(body.firstName).build(),
                            AttributeType.builder().name("family_name").value(body.lastName).build(),
                            AttributeType.builder().name("custom:role").value(body.role).build()
                    )
                    .messageAction(MessageActionType.SUPPRESS)
                    .temporaryPassword(body.password)
                    .build();

            AdminCreateUserResponse cognitoResponse = cognitoClient.adminCreateUser(cognitoRequest);

            // Set permanent password
            AdminSetUserPasswordRequest setPasswordRequest = AdminSetUserPasswordRequest.builder()
                    .userPoolId(userPoolId)
                    .username(body.email)
                    .password(body.password)
                    .permanent(true)
                    .build();

            cognitoClient.adminSetUserPassword(setPasswordRequest);

            // Create travel agent record in DynamoDB
            TravelAgent agent = new TravelAgent();
            agent.setEmail(body.email);
            agent.setFirstName(body.firstName);
            agent.setLastName(body.lastName);
            agent.setRole(body.role);
            agent.setCreatedAt(Instant.now().toString());
            agent.setCreatedBy(callerEmail);
            agent.setPhone(body.phone);
            agent.setMessenger(body.messenger);

            agentsRepo.save(agent);

            log.info("Travel agent created: {} by admin: {}", body.email, callerEmail);
            return HttpResponses.json(mapper, 201, new CreateTravelAgentResponse(body.email, "Travel agent created successfully"));

        } catch (UsernameExistsException e) {
            return HttpResponses.error(mapper, 409, "user with this email already exists");
        } catch (InvalidPasswordException e) {
            return HttpResponses.error(mapper, 400, "invalid password: " + e.getMessage());
        } catch (Exception e) {
            log.error("createTravelAgent failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent listTravelAgents(APIGatewayProxyRequestEvent event) {
        try {
            // Auth: require ADMIN role
            String callerEmail = extractClaim(event, "email");
            String callerRole = extractClaim(event, "custom:role");

            if (callerEmail == null || callerEmail.isBlank()) {
                return HttpResponses.error(mapper, 401, "authentication required");
            }

            if (!"ADMIN".equals(callerRole)) {
                return HttpResponses.error(mapper, 403, "only admins can list travel agents");
            }

            // Verify admin exists
            TravelAgent admin = agentsRepo.findByEmail(callerEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return HttpResponses.error(mapper, 403, "not a registered admin");
            }

            // List all agents
            List<TravelAgent> agents = agentsRepo.findAll();

            // Convert to DTOs
            List<ListTravelAgentsResponse.TravelAgentDTO> agentDTOs = agents.stream()
                    .map(agent -> {
                        ListTravelAgentsResponse.TravelAgentDTO dto = new ListTravelAgentsResponse.TravelAgentDTO();
                        dto.email = agent.getEmail();
                        dto.firstName = agent.getFirstName();
                        dto.lastName = agent.getLastName();
                        dto.role = agent.getRole();
                        dto.createdAt = agent.getCreatedAt();
                        dto.createdBy = agent.getCreatedBy();
                        dto.phone = agent.getPhone();
                        dto.messenger = agent.getMessenger();
                        return dto;
                    })
                    .collect(Collectors.toList());

            return HttpResponses.json(mapper, 200, new ListTravelAgentsResponse(agentDTOs));

        } catch (Exception e) {
            log.error("listTravelAgents failed", e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    @Override
    public APIGatewayProxyResponseEvent deleteTravelAgent(APIGatewayProxyRequestEvent event, String email) {
        try {
            // Auth: require ADMIN role
            String callerEmail = extractClaim(event, "email");
            String callerRole = extractClaim(event, "custom:role");

            if (callerEmail == null || callerEmail.isBlank()) {
                return HttpResponses.error(mapper, 401, "authentication required");
            }

            if (!"ADMIN".equals(callerRole)) {
                return HttpResponses.error(mapper, 403, "only admins can delete travel agents");
            }

            // Verify admin exists
            TravelAgent admin = agentsRepo.findByEmail(callerEmail);
            if (admin == null || !"ADMIN".equals(admin.getRole())) {
                return HttpResponses.error(mapper, 403, "not a registered admin");
            }

            // Check if agent exists
            TravelAgent agent = agentsRepo.findByEmail(email);
            if (agent == null) {
                return HttpResponses.error(mapper, 404, "travel agent not found");
            }

            // Prevent deleting self
            if (email.equalsIgnoreCase(callerEmail)) {
                return HttpResponses.error(mapper, 400, "cannot delete your own account");
            }

            // Delete from Cognito
            try {
                cognitoClient.adminDeleteUser(AdminDeleteUserRequest.builder()
                        .userPoolId(userPoolId)
                        .username(email)
                        .build());
            } catch (Exception e) {
                log.warn("Failed to delete user from Cognito: {}", email, e);
                // Continue to delete from DynamoDB anyway
            }

            // Delete from DynamoDB
            agentsRepo.deleteByEmail(email);

            log.info("Travel agent deleted: {} by admin: {}", email, callerEmail);
            return HttpResponses.json(mapper, 200, new DeleteTravelAgentResponse("Travel agent deleted successfully"));

        } catch (Exception e) {
            log.error("deleteTravelAgent failed for email={}", email, e);
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    private static String extractClaim(APIGatewayProxyRequestEvent event, String claimName) {
        if (event == null || event.getRequestContext() == null) return null;
        var auth = event.getRequestContext().getAuthorizer();
        if (auth == null) return null;
        Object claimsObj = auth.get("claims");
        if (!(claimsObj instanceof java.util.Map)) return null;
        Object v = ((java.util.Map<?, ?>) claimsObj).get(claimName);
        return v == null ? null : v.toString();
    }
}

