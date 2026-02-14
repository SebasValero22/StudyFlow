package com.iescamp.studyflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.iescamp.studyflow.model.User;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class UserService {
    private final HttpClient client = HttpClient.newHttpClient();
    // Nota: Cambiado "." por ":" en el puerto y "https" por "http" si estás en local
    private final String API_URL = "http://localhost:8888/api/users";
    private final ObjectMapper mapper = new ObjectMapper();

    public List<User> getAllUsers() throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(API_URL))
                .header("Accept", "application/json") // Especifica el formato JSON [cite: 23]
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        // Verificamos el código de estado HTTP antes de parsear [cite: 48, 210]
        if (response.statusCode() == 200) {
            return parseJsonToUserList(response.body());
        } else {
            throw new IOException("Error del servidor: " + response.statusCode());
        }
    }

    private List<User> parseJsonToUserList(String json) throws IOException {
        // Esta línea hace la magia de convertir el JSON en una lista de objetos User
        return mapper.readValue(json, new TypeReference<List<User>>() {});
    }
}