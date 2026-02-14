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

    public static HttpResponse<String> sendRequest(HttpRequest request) throws Exception {
        return client.send(request, HttpResponse.BodyHandlers.ofString());
    }

    // Método de ayuda para construir peticiones rápidamente
    public static HttpRequest.Builder prepareRequest(String endpoint) {
        return HttpRequest.newBuilder()
                .uri(URI.create(ConfigLoader.getApiUrl() + endpoint))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json");
    }
}