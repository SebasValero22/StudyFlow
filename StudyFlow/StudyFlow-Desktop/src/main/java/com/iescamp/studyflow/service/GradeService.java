package com.iescamp.studyflow.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iescamp.studyflow.model.Grade;
import com.iescamp.studyflow.utils.JsonMapper;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class GradeService {

    // GET /api/grades
    public List<Grade> getAllGrades() throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/grades").GET().build();
        HttpResponse<String> response = ApiClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), new TypeReference<List<Grade>>() {});
        }
        throw new Exception("ERROR LOADING EXAMS ");
    }

    // POST /api/grades
    public void saveGrade(Grade grade) throws Exception {
        String json = JsonMapper.get().writeValueAsString(grade);
        HttpRequest request = ApiClient.prepareRequest("/grades")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("ERROR SAVING GRADE");
        }
    }

    // PUT /api/grades/{id}
    public void updateGrade(Integer id, Grade grade) throws Exception {
        String json = JsonMapper.get().writeValueAsString(grade);
        HttpRequest request = ApiClient.prepareRequest("/grades/" + id)
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("ERROR UPDATING GRADE");
        }
    }

    // DELETE /api/grades/{id}
    public void deleteGrade(Integer id) throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/grades/" + id).DELETE().build();
        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("ERROR DELETING GRADE");
        }
    }
}