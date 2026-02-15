package com.iescamp.studyflow.service;


import com.fasterxml.jackson.core.type.TypeReference;
import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.utils.JsonMapper;

import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;

public class TaskService {

    // GET /api/tasks
    public List<Task> getAllTasks() throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/tasks").GET().build();
        HttpResponse<String> response = ApiClient.sendRequest(request);

        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), new TypeReference<List<Task>>(){});
        }
        throw new Exception("ERROR LOADING THE TASK  ");
    }

    // POST /api/tasks
    public void saveTask(Task task) throws Exception {
        String json = JsonMapper.get().writeValueAsString(task);
        HttpRequest request = ApiClient.prepareRequest("/tasks")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("TASK COULDNT BE SAVED ");
        }
    }

    // PUT /api/tasks/{id}
    public void updateTask(Integer id, Task task) throws Exception {
        String json = JsonMapper.get().writeValueAsString(task);
        HttpRequest request = ApiClient.prepareRequest("/tasks/" + id)
                .PUT(HttpRequest.BodyPublishers.ofString(json)).build();

        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("ERROR UPDATING TASK ");
        }
    }

    // DELETE /api/tasks/{id}
    public void deleteTask(Integer id) throws Exception {
        HttpRequest request = ApiClient.prepareRequest("/tasks/" + id).DELETE().build();
        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("ERROR DELETING TASK ");
        }
    }
}