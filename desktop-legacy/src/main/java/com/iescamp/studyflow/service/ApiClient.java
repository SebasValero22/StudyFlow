package com.iescamp.studyflow.service;

import com.iescamp.studyflow.utils.AppSettings;
import com.iescamp.studyflow.utils.ConfigLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;

public class ApiClient {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    private static volatile String jwtToken = null;

    public static void setToken(String token) {
        jwtToken = token;
        long exp = decodeExpMs(token);
        if (exp > 0) {
            AppSettings.get().setTokenExpiresAt(exp);
        } else {
            AppSettings.get().clearTokenExpiresAt();
        }
    }

    public static void clearToken() {
        jwtToken = null;
        AppSettings.get().clearTokenExpiresAt();
    }

    public static String getToken() {
        return jwtToken;
    }

    /** True when a token exists and has not passed its JWT exp claim. */
    public static boolean isTokenUsable() {
        String token = jwtToken;
        if (token == null || token.isBlank()) return false;
        long exp = AppSettings.get().getTokenExpiresAt();
        if (exp <= 0) return true; // no exp claim — let the server decide
        return System.currentTimeMillis() < exp;
    }

    /** Parse JWT payload exp (seconds → ms). Returns 0 if unparseable. */
    static long decodeExpMs(String token) {
        try {
            if (token == null) return 0;
            String[] parts = token.split("\\.");
            if (parts.length < 2) return 0;
            String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
            com.fasterxml.jackson.databind.JsonNode node =
                    com.iescamp.studyflow.utils.JsonMapper.get().readTree(payload);
            if (node != null && node.has("exp")) {
                return node.get("exp").asLong() * 1000L;
            }
        } catch (Exception ignored) {
            // not a parseable JWT — fall through
        }
        return 0;
    }

    public static HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        if (response.statusCode() == 401) {
            // Token expired or revoked mid-session: drop it so sync/API callers stop retrying blindly.
            clearToken();
        }
        return response;
    }

    public static HttpRequest.Builder prepareRequest(String endpoint) {
        HttpRequest.Builder builder = HttpRequest.newBuilder()
                .uri(URI.create(ConfigLoader.getApiUrl() + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");

        String token = jwtToken;
        if (token != null && !token.isEmpty()) {
            builder.header("Authorization", "Bearer " + token);
        }

        return builder;
    }
}
