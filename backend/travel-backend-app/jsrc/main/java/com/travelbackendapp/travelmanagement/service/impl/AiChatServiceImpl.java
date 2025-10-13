// service/impl/AiChatServiceImpl.java
package com.travelbackendapp.travelmanagement.service.impl;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travelbackendapp.travelmanagement.controller.GeminiClient;
import com.travelbackendapp.travelmanagement.model.entity.TourItem;
import com.travelbackendapp.travelmanagement.repository.ToursRepository;
import com.travelbackendapp.travelmanagement.service.AiChatService;
import com.travelbackendapp.travelmanagement.util.HttpResponses;
import com.travelbackendapp.travelmanagement.util.TourSnippets;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

public class AiChatServiceImpl implements AiChatService {

    private final ObjectMapper mapper;
    private final ToursRepository toursRepo;
    private final GeminiClient gemini;

    // ---------- DTOs ----------

    static class ChatRequest { public String message; }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class ChatResponse {
        public String reply;
        public List<Card> cards = new ArrayList<>(); // ALWAYS non-null
    }

    @JsonInclude(JsonInclude.Include.NON_NULL)
    static class Card {
        public String id;
        public String title;       // tour name
        public String destination;
        public String hotel;       // hotel name (optional)
        public String badge;       // e.g., "2025-07-12 • $1,999"
        public String imageUrl;    // first image if present
        public Double priceFrom;   // numeric for UI sorting
        public String durationHint;
        public String mealHint;
        public String rating;      // formatted "4.5"
    }

    // ---------- DI ----------

    @Inject
    public AiChatServiceImpl(ObjectMapper mapper,
                             ToursRepository toursRepo,
                             @Named("GEMINI_API_KEY") String apiKey,
                             @Named("GEMINI_MODEL") String model) {
        this.mapper = mapper;
        this.toursRepo = toursRepo;
        // matches your GeminiClient(apiKey, mapper, model) constructor
        this.gemini = new GeminiClient(apiKey, mapper, model);
    }

    // ---------- Public entry ----------

    @Override
    public APIGatewayProxyResponseEvent chat(APIGatewayProxyRequestEvent event) {
        try {
            ChatRequest req = mapper.readValue(
                    event.getBody() == null ? "{}" : event.getBody(), ChatRequest.class);

            String userMsg = (req.message == null ? "" : req.message.trim());
            if (userMsg.isEmpty()) {
                return HttpResponses.error(mapper, 400, "message is required");
            }

            // 1) Decide intent FIRST (strict: only clear “suggest” phrasing triggers DB)
            boolean wantsSuggestions = looksLikeSuggestionsIntent(userMsg);

            if (!wantsSuggestions) {
                // General travel chat -> LLM only, no DB hit
                String reply = normalizeReply(llmGeneralAnswer(userMsg));
                ChatResponse resp = new ChatResponse();
                resp.reply = reply;
                return HttpResponses.json(mapper, 200, resp);
            }

            // 2) Suggestions -> fetch, rank, cards + concise LLM summary
            List<TourItem> all = toursRepo.listAll();
            if (all.isEmpty()) {
                // No inventory: still give useful general guidance instead of dead-end
                String reply = normalizeReply(llmNoInventoryAdvice(userMsg));
                ChatResponse resp = new ChatResponse();
                resp.reply = reply;        // cards remains []
                return HttpResponses.json(mapper, 200, resp);
            }

            List<TourItem> ranked = rankForQuery(all, userMsg)
                    .stream().limit(12).collect(Collectors.toList());

            String compact = ranked.stream()
                    .limit(6)
                    .map(TourSnippets::compactLine)
                    .collect(Collectors.joining("\n"));

            String systemGuardrail =
                    "You are a travel assistant for a tour booking app. " +
                            "Only answer travel-related queries (destinations, tours, dates, durations, meal plans, budgets, guests). " +
                            "If the user asks something non-travel, politely refuse and steer back to travel topics.";

            String task =
                    "User: " + userMsg + "\n\n" +
                            "Available matches (subset):\n" + compact + "\n\n" +
                            "Write a friendly, concise summary (3–6 short bullet points) referencing a few tours by name and destination. " +
                            "Avoid markdown tables and code fences. Do NOT invent IDs or prices; use only what is provided above. " +
                            "Keep it under 120 words.";

            String reply = normalizeReply(gemini.generateText(systemGuardrail + "\n\n" + task));

            ChatResponse resp = new ChatResponse();
            resp.reply = reply;
            resp.cards = toCards(ranked);
            return HttpResponses.json(mapper, 200, resp);

        } catch (Exception e) {
            return HttpResponses.error(mapper, 500, "internal server error");
        }
    }

    // ---------- Intent detection ----------
    // We ONLY consider these as "suggestions" when the user clearly asks for recommendations/options.
    // Mentions of meal plans (AI/HB/FB), destinations, or generic travel words ALONE do NOT trigger.
    private static boolean looksLikeSuggestionsIntent(String userMsg) {
        String s = userMsg.toLowerCase(Locale.ROOT);

        // verbs/phrases that explicitly request options
        boolean suggestVerb =
                s.matches(".*\\b(recommend|recommendations?|suggest|suggestions?|options?|shortlist|what (are|would be) (some|good)|where should i go|find (me )?tours?|show (me )?(tours|options))\\b.*");

        // signals that the user is filtering a search (budget/duration/party size),
        // but we still require some “tour choice” framing words to avoid false positives.
        boolean filterSignal =
                s.matches(".*\\b(\\$\\d+[\\w ]*|under \\$?\\d+|below \\$?\\d+|up to \\$?\\d+)\\b.*") || // budget patterns
                        s.matches(".*\\b(\\d+\\s*(day|days|night|nights))\\b.*") ||                               // duration patterns
                        s.matches(".*\\b(adults?|children|kids?)\\b.*");                                        // party size

        boolean tourFraming =
                s.matches(".*\\b(tour|tours|package|packages|itinerary|itineraries|resort|resorts|hotel|hotels|trip|trips)\\b.*") ||
                        s.matches(".*\\bfor (a|our|my) trip\\b.*");

        // Intent: either explicit suggestion verb, or (filters + tour framing)
        return suggestVerb || (filterSignal && tourFraming);
    }

    // ---------- LLM prompts ----------

    private String llmGeneralAnswer(String userMsg) {
        String systemGuardrail =
                "You are a travel assistant for a tour booking app. " +
                        "Only answer travel-related queries (destinations, tours, dates, durations, meal plans, budgets, guests). " +
                        "If the user asks something non-travel, politely refuse and steer back to travel topics.";
        String task =
                "User: " + userMsg + "\n\n" +
                        "Answer clearly and helpfully. Use short paragraphs or 3–8 compact bullet points where useful. " +
                        "Avoid code fences and tables. Provide practical, actionable travel guidance.";
        return gemini.generateText(systemGuardrail + "\n\n" + task);
    }

    private String llmNoInventoryAdvice(String userMsg) {
        String systemGuardrail =
                "You are a travel assistant for a tour booking app. " +
                        "Only answer travel-related queries (destinations, tours, dates, durations, meal plans, budgets, guests).";
        String task =
                "The user asked for tour recommendations but there are currently no tours in the catalog.\n" +
                        "User: " + userMsg + "\n\n" +
                        "Write 3–6 short bullet points with genuinely helpful guidance (destinations to consider, " +
                        "timing tips, where AI/HB/FB fits, typical price ranges), and invite them to try again later. " +
                        "Avoid code fences and tables.";
        return gemini.generateText(systemGuardrail + "\n\n" + task);
    }

    // ---------- Ranking & cards ----------

    private static List<TourItem> rankForQuery(List<TourItem> all, String userMsg) {
        List<String> terms = Arrays.stream(userMsg.split("\\W+"))
                .filter(w -> w != null && w.length() > 2)
                .limit(10)
                .collect(Collectors.toList());

        return all.stream()
                .sorted((a, b) -> Integer.compare(
                        TourSnippets.keywordScore(b, terms),
                        TourSnippets.keywordScore(a, terms)
                ))
                .collect(Collectors.toList());
    }

    private static List<Card> toCards(List<TourItem> tours) {
        List<Card> out = new ArrayList<>();
        for (TourItem t : tours) {
            Card c = new Card();
            c.id           = safe(t.getTourId());
            c.title        = safe(t.getName());
            c.destination  = safe(t.getDestination());
            c.hotel        = safe(t.getHotelName());
            c.badge        = TourSnippets.shortBadge(t);
            c.imageUrl     = TourSnippets.mainImage(t);
            c.priceFrom    = t.getPriceFrom();
            c.durationHint = joinOrNull(t.getDurations(), "/");
            c.mealHint     = joinOrNull(t.getMealPlans(), "/");
            c.rating       = t.getRating() == null ? null : String.format(Locale.US, "%.1f", t.getRating());
            out.add(c);
        }
        return out;
    }

    // ---------- Utils ----------

    private static String joinOrNull(List<String> l, String sep) {
        return (l == null || l.isEmpty()) ? null : String.join(sep, l);
    }

    private static String safe(String s) { return s == null ? "" : s; }

    // Remove code fences, collapse whitespace, and standardize list bullets
    private static String normalizeReply(String raw) {
        if (raw == null) return "";
        String s = raw.trim();

        // strip ``` blocks if present
        if (s.startsWith("```")) {
            int idx = s.indexOf('\n');
            if (idx > 0) s = s.substring(idx + 1);
            if (s.endsWith("```")) s = s.substring(0, s.length() - 3);
            s = s.trim();
        }

        // normalize newlines and collapse >2 blank lines
        s = s.replace("\r\n", "\n").replace("\r", "\n");
        s = s.replaceAll("\n{3,}", "\n\n");

        // standardize bullet prefix to "- "
        String[] lines = s.split("\n");
        StringBuilder out = new StringBuilder();
        for (String line : lines) {
            String t = line.trim();
            if (t.startsWith("* ")) t = "- " + t.substring(2);
            else if (t.startsWith("• ")) t = "- " + t.substring(2);
            out.append(t).append("\n");
        }
        return out.toString().trim();
    }
}
