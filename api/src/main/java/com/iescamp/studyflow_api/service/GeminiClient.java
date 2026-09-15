package com.iescamp.studyflow_api.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

/**
 * Thin wrapper around the Gemini generateContent API that always requests
 * strict JSON output. Mock-aware: returns deterministic canned JSON when no
 * real API key is configured, so local dev and tests work offline.
 */
@Component
public class GeminiClient {

    private final RestTemplate restTemplate = new RestTemplate();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Value("${nyra.gemini.url:https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent}")
    private String endpoint;

    @Value("${nyra.gemini.key:MOCK_KEY}")
    private String apiKey;

    /**
     * @return raw model text (JSON string), or "{}" on failure.
     */
    public String completeJson(String prompt) {
        if ("MOCK_KEY".equals(apiKey)) {
            return mockResponse(prompt);
        }

        try {
            String url = endpoint + "?key=" + apiKey;
            Map<String, Object> body = Map.of(
                    "contents", List.of(Map.of("parts", List.of(Map.of("text", prompt)))),
                    "generationConfig", Map.of("responseMimeType", "application/json")
            );

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            ResponseEntity<Map> response = restTemplate.postForEntity(url, new HttpEntity<>(body, headers), Map.class);

            if (response.getStatusCode() == HttpStatus.OK && response.getBody() != null) {
                JsonNode root = objectMapper.valueToTree(response.getBody());
                JsonNode text = root.path("candidates").path(0).path("content").path("parts").path(0).path("text");
                if (text.isTextual()) {
                    String out = text.asText().trim();
                    if (out.startsWith("```json")) {
                        out = out.substring(7, out.length() - 3).trim();
                    }
                    return out;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "{}";
    }

    private String mockResponse(String prompt) {
        if (prompt.contains("VOICE JOURNAL CLEANUP")) {
            return "{\"title\":\"Journal Entry\",\"transcript\":\"# Journal Entry\\n\\n- [ ] Review captured thoughts\",\"moodTag\":\"NEUTRAL\"}";
        }
        return "{}";
    }
}
