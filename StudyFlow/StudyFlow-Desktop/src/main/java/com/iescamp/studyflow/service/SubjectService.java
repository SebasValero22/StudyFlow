package com.iescamp.studyflow.service;

import com.iescamp.studyflow.model.Subject;
import com.iescamp.studyflow.utils.JsonMapper; // Tu mapper
import com.fasterxml.jackson.core.type.TypeReference;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Arrays;
import java.util.List;

public class SubjectService {

    // GET /api/subjects
    public List<Subject> getAllSubjects() throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/subjects").GET().build();
        HttpResponse<String> response = ApiClient.sendRequest(request);

        if (response.statusCode() == 200) {
            // Truco: Convertimos a Array primero para evitar problemas de tipos genéricos con Jackson
            Subject[] subjects = JsonMapper.get().readValue(response.body(), Subject[].class);
            return Arrays.asList(subjects);
        }
        throw new Exception("Error loading subjects: " + response.statusCode());
    }

    // POST /api/subjects
    public void saveSubject(Subject subject) throws Exception {
        String json = JsonMapper.get().writeValueAsString(subject);
        HttpRequest request = ApiClient.prepareRequest("/subjects")
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        // CORRECCIÓN: Si es distinto de 200/201 es cuando hay error
        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("Subject couldn't be saved");
        }
    }

    // UPDATE /api/subjects/{id}
    public void updateSubject(Integer id, Subject subject) throws Exception {
        String json = JsonMapper.get().writeValueAsString(subject);
        HttpRequest request = ApiClient.prepareRequest("/subjects/" + id) // Corregido endpoint
                .PUT(HttpRequest.BodyPublishers.ofString(json))
                .build();

        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("Subject couldn't be updated");
        }
    }

    // DELETE /api/subjects/{id}
    public void deleteSubject(Integer id) throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/subjects/" + id).DELETE().build();

        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("Subject couldn't be deleted");
        }
    }
}