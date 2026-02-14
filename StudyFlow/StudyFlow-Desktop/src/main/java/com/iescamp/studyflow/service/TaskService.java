package com.iescamp.studyflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.utils.JsonMapper;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TaskService {

    public List<Task> getAllTasks() throws Exception {
        // Preparamos la petición usando tu ApiClient genérico [cite: 23, 117]
        HttpRequest request = ApiClient.prepareRequest("/tasks")
                .GET()
                .build();

        HttpResponse<String> response = ApiClient.sendRequest(request);

        // Interpretación del código de estado HTTP (RA 1.37 / 2.10)
        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), new TypeReference<List<Task>>() {});
        } else {
            throw new Exception("Error del servidor: " + response.statusCode());
        }
    }

    public void saveTask(Task task) throws Exception {
        String json = JsonMapper.get().writeValueAsString(task);

        HttpRequest request = ApiClient.prepareRequest("/tasks")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        HttpResponse<String> response = ApiClient.sendRequest(request);

        if (response.statusCode() != 201) {
            throw new Exception("No se pudo crear la tarea. Código: " + response.statusCode());
        }
    }
}