package com.example.studyflow.data.repository;

import com.example.studyflow.data.dto.SubjectResponseDTO;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
import com.example.studyflow.utils.UserSession;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubjectRepository {
    private ApiService apiService;

    public SubjectRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getSubjects(TaskRepository.RepositoryCallback<List<SubjectResponseDTO>> callback) {
        Integer userId = UserSession.getInstance().isLoggedIn() ? UserSession.getInstance().getUser().getUserId() : null;
        apiService.getSubjects(userId).enqueue(new Callback<List<SubjectResponseDTO>>() {
            @Override
            public void onResponse(Call<List<SubjectResponseDTO>> call, Response<List<SubjectResponseDTO>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error fetching subjects");
                }
            }

            @Override
            public void onFailure(Call<List<SubjectResponseDTO>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void createSubject(SubjectResponseDTO subject, TaskRepository.RepositoryCallback<SubjectResponseDTO> callback) {
        apiService.createSubject(subject).enqueue(new Callback<SubjectResponseDTO>() {
            @Override
            public void onResponse(Call<SubjectResponseDTO> call, Response<SubjectResponseDTO> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error creating subject");
                }
            }

            @Override
            public void onFailure(Call<SubjectResponseDTO> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void updateSubject(Integer id, SubjectResponseDTO subject, TaskRepository.RepositoryCallback<SubjectResponseDTO> callback) {
        apiService.updateSubject(id, subject).enqueue(new Callback<SubjectResponseDTO>() {
            @Override
            public void onResponse(Call<SubjectResponseDTO> call, Response<SubjectResponseDTO> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error updating subject");
                }
            }

            @Override
            public void onFailure(Call<SubjectResponseDTO> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }

    public void deleteSubject(Integer id, TaskRepository.RepositoryCallback<Void> callback) {
        apiService.deleteSubject(id).enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(null);
                } else {
                    callback.onError("Error deleting subject");
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
