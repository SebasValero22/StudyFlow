package com.iescamp.studyflow.service;

import com.iescamp.studyflow.utils.ConfigLoader;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class ApiClient {
    private static final HttpClient client = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10)) // Gestión de tiempos de espera
            .build();

    private static volatile String jwtToken = null;

    public static void setToken(String token) {
        jwtToken = token;
    }

    public static void clearToken() {
        jwtToken = null;
    }

    public static String getToken() {
        return jwtToken;
    }

    public static HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Método de ayuda para construir peticiones rápidamente
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
