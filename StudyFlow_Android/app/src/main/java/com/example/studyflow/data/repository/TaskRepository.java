package com.example.studyflow.data.repository;

import com.example.studyflow.data.model.Task;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskRepository {
    private final ApiService apiService;

    public TaskRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchTasks(TasksCallback callback) {
        apiService.getTasks().enqueue(new Callback<List<Task>>() {
            @Override
            public void onResponse(Call<List<Task>> call, Response<List<Task>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al obtener tareas");
                }
            }

            @Override
            public void onFailure(Call<List<Task>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface TasksCallback {
        void onSuccess(List<Task> tasks);
        void onError(String message);
    }
}