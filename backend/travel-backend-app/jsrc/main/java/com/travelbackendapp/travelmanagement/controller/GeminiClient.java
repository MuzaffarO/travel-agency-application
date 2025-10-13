// controller/GeminiClient.java
package com.travelbackendapp.travelmanagement.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.*;
import java.time.Duration;
import java.util.List;
import java.util.Map;

public class GeminiClient {
    private static final Logger log = LoggerFactory.getLogger(GeminiClient.class);

    private final String apiKey;
    private final String model;
    private final ObjectMapper mapper;
    private final HttpClient http;

    public GeminiClient(String apiKey, ObjectMapper mapper, String model) {
        this.apiKey = apiKey;
        this.model = (model == null || model.isBlank()) ? "gemini-2.0-flash" : model.trim();
        this.mapper = mapper;
        this.http = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(8)).build();
    }

    public String generateText(String prompt) {
        if (apiKey == null || apiKey.isBlank()) {
            log.error("Gemini API key missing");
            return "AI is temporarily unavailable (missing API key).";
        }
        try {
            String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                    + model + ":generateContent?key=" + apiKey;

            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("temperature", 0.4, "maxOutputTokens", 512)
            );

            String json = mapper.writeValueAsString(body);
            HttpRequest req = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofSeconds(20))
                    .header("Content-Type", "application/json; charset=UTF-8")
                    .POST(HttpRequest.BodyPublishers.ofString(json))
                    .build();

            HttpResponse<String> resp = http.send(req, HttpResponse.BodyHandlers.ofString());
            if (resp.statusCode() / 100 != 2) {
                log.error("Gemini HTTP {}: {}", resp.statusCode(), resp.body());
                return "Sorry, I couldn’t generate a response right now.";
            }

            // Parse: candidates[0].content.parts[*].text
            Map<?, ?> root = mapper.readValue(resp.body(), Map.class);
            Object candidates = root.get("candidates");
            if (!(candidates instanceof List) || ((List<?>) candidates).isEmpty()) {
                log.error("Gemini response missing candidates: {}", resp.body());
                return "Sorry, I couldn’t generate a response right now.";
            }
            Map<?, ?> cand0 = (Map<?, ?>) ((List<?>) candidates).get(0);
            Map<?, ?> content = (Map<?, ?>) cand0.get("content");
            if (content == null) return "Sorry, I couldn’t generate a response right now.";
            Object parts = content.get("parts");
            if (!(parts instanceof List) || ((List<?>) parts).isEmpty()) {
                return "Sorry, I couldn’t generate a response right now.";
            }
            StringBuilder out = new StringBuilder();
            for (Object p : (List<?>) parts) {
                if (p instanceof Map) {
                    Object t = ((Map<?, ?>) p).get("text");
                    if (t != null) out.append(t.toString());
                }
            }
            String res = out.toString().trim();
            return res.isEmpty() ? "Sorry, I couldn’t generate a response right now." : res;

        } catch (Exception e) {
            log.error("Gemini call failed", e);
            return "Sorry, I couldn’t generate a response right now.";
        }
    }
}
