package com.iescamp.studyflow.service;
import com.fasterxml.jackson.core.type.TypeReference;
import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.utils.JsonMapper;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class ExamService {

    // GET /api/exams
    public List<Exam> getAllExams() throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/exams").GET().build();
        HttpResponse<String> response = ApiClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), new TypeReference<List<Exam>>() {});
        }
        throw new Exception("ERROR LOADING EXAMS ");
    }

    // GET /api/exams/subject/{id} (Muy útil para filtrar en el UI)
    public List<Exam> getExamsBySubject(Integer subjectId) throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/exams/subject/" + subjectId).GET().build();
        HttpResponse<String> response = ApiClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), new TypeReference<List<Exam>>() {});
        }
        throw new Exception("ERROR FILTERING EXAMS ");
    }

    // POST /api/exams
    public void saveExam(Exam exam) throws Exception {
        String json = JsonMapper.get().writeValueAsString(exam);
        HttpRequest request = ApiClient.prepareRequest("/exams")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("EXAM COULDNT BE UPDATED");
        }
    }

    // DELETE /api/exams/{id}
    public void deleteExam(Integer id) throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/exams/" + id).DELETE().build();
        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("EXAM COULDNT BE DELETED");
        }
    }
}