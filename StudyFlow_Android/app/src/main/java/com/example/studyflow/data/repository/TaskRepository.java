package com.example.studyflow.data.repository;

import com.example.studyflow.data.dto.TaskResponseDTO;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TaskRepository {
    private ApiService apiService;

    public TaskRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getTasks(RepositoryCallback<List<TaskResponseDTO>> callback) {
        apiService.getTasks().enqueue(new Callback<List<TaskResponseDTO>>() {
            @Override
            public void onResponse(Call<List<TaskResponseDTO>> call, Response<List<TaskResponseDTO>> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error fetching tasks");
            }
            @Override
            public void onFailure(Call<List<TaskResponseDTO>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createTask(TaskResponseDTO task, RepositoryCallback<TaskResponseDTO> callback) {
        apiService.createTask(task).enqueue(new Callback<TaskResponseDTO>() {
            @Override
            public void onResponse(Call<TaskResponseDTO> call, Response<TaskResponseDTO> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error creating task");
            }
            @Override
            public void onFailure(Call<TaskResponseDTO> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateTask(Integer id, TaskResponseDTO task, RepositoryCallback<TaskResponseDTO> callback) {
        apiService.updateTask(id, task).enqueue(new Callback<TaskResponseDTO>() {
            @Override
            public void onResponse(Call<TaskResponseDTO> call, Response<TaskResponseDTO> response) {
                if (response.isSuccessful()) callback.onSuccess(response.body());
                else callback.onError("Error updating task");
            }
            @Override
            public void onFailure(Call<TaskResponseDTO> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteTask(Integer id, RepositoryCallback<Void> callback) {
        apiService.deleteTask(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) callback.onSuccess(null);
                else callback.onError("Error deleting task");
            }
            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public interface RepositoryCallback<T> {
        void onSuccess(T result);
        void onError(String message);
    }
}
