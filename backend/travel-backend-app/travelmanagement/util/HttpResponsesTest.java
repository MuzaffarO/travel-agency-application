package com.travelbackendapp.travelmanagement.util;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HttpResponses Tests")
class HttpResponsesTest {

    private ObjectMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new ObjectMapper();
    }

    @Test
    @DisplayName("Should create JSON response successfully")
    void shouldCreateJsonResponseSuccessfully() throws Exception {
        // Given
        Map<String, String> body = Map.of("message", "success");

        // When
        APIGatewayProxyResponseEvent response = HttpResponses.json(mapper, 200, body);

        // Then
        assertNotNull(response);
        assertEquals(200, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getHeaders().containsKey("Content-Type"));
        assertEquals("application/json", response.getHeaders().get("Content-Type"));
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should create error response")
    void shouldCreateErrorResponse() throws Exception {
        // When
        APIGatewayProxyResponseEvent response = HttpResponses.error(mapper, 400, "Bad request");

        // Then
        assertNotNull(response);
        assertEquals(400, response.getStatusCode());
        assertNotNull(response.getBody());
        assertTrue(response.getBody().contains("error"));
        assertTrue(response.getBody().contains("Bad request"));
    }

    @Test
    @DisplayName("Should create empty response")
    void shouldCreateEmptyResponse() {
        // When
        APIGatewayProxyResponseEvent response = HttpResponses.empty(204);

        // Then
        assertNotNull(response);
        assertEquals(204, response.getStatusCode());
        assertTrue(response.getHeaders().containsKey("Access-Control-Allow-Origin"));
    }

    @Test
    @DisplayName("Should handle serialization error gracefully")
    void shouldHandleSerializationErrorGracefully() {
        // Given - ObjectMapper that will fail
        ObjectMapper failingMapper = new ObjectMapper() {
            @Override
            public String writeValueAsString(Object value) {
                throw new RuntimeException("Serialization failed");
            }
        };

        // When
        APIGatewayProxyResponseEvent response = HttpResponses.json(failingMapper, 200, new Object());

        // Then
        assertNotNull(response);
        assertEquals(500, response.getStatusCode());
        assertNotNull(response.getBody());
    }
}

