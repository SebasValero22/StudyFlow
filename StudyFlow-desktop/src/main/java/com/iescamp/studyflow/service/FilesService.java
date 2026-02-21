package com.iescamp.studyflow.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.iescamp.studyflow.model.Files;
import com.iescamp.studyflow.utils.JsonMapper;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class FilesService {

    // GET /api/files
    public List<Files> getAllFiles() throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/files").GET().build();
        HttpResponse<String> response = ApiClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), new TypeReference<List<Files>>(){});
        }
        throw new Exception("ERROR LOADING FILES: " + response.statusCode());
    }

    // DELETE /api/files/{id}
    public void deleteFile(Integer id) throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/files/" + id).DELETE().build();
        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("FILE COULDNT BE DELETED");
        }
    }

    // POST /api/files (CORREGIDO)
    // Nota: Recibe tu modelo 'Files', no 'java.io.File'
    public void addFiles(Files fileModel) throws Exception {
        String json = JsonMapper.get().writeValueAsString(fileModel);

        HttpRequest request = ApiClient.prepareRequest("/files")
                .POST(HttpRequest.BodyPublishers.ofString(json)) // Faltaba esto
                .build();

        if (ApiClient.sendRequest(request).statusCode() != 200) { // O 201
            throw new Exception("FILE COULDNT BE SAVED");
        }
    }
}