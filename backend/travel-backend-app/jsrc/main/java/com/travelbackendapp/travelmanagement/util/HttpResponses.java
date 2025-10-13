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
            return error(mapper, 500, "serialization error");
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
