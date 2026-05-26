package com.travelbackendapp.travelmanagement.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.util.Map;

public final class HttpResponses {
    private static final Map<String, String> JSON = Map.of(
            "Content-Type", "application/json",
            "Access-Control-Allow-Origin", "*",
            "Access-Control-Allow-Methods", "GET,POST,OPTIONS",
            "Access-Control-Allow-Headers", "Content-Type, Authorization"
    );

    private HttpResponses() {}

    public static APIGatewayProxyResponseEvent json(ObjectMapper mapper, int status, Object bodyObj) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(status)
                    .withHeaders(JSON)
                    .withBody(mapper.writeValueAsString(bodyObj));
        } catch (Exception e) {
            // Avoid infinite recursion - directly create error response
            try {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(500)
                        .withHeaders(JSON)
                        .withBody("{\"error\":\"serialization error\"}");
            } catch (Exception e2) {
                return new APIGatewayProxyResponseEvent()
                        .withStatusCode(500)
                        .withHeaders(JSON)
                        .withBody("{\"error\":\"internal server error\"}");
            }
        }
    }

    public static APIGatewayProxyResponseEvent error(ObjectMapper mapper, int status, String message) {
        return json(mapper, status, Map.of("error", message));
    }

    /** Handy for preflight. */
    public static APIGatewayProxyResponseEvent empty(int status) {
        return new APIGatewayProxyResponseEvent().withStatusCode(status).withHeaders(JSON);
    }
}
