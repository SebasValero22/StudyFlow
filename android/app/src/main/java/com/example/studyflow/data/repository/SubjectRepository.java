package com.example.studyflow.data.repository;

import com.example.studyflow.data.dto.SubjectResponseDTO;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
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
        apiService.getSubjects().enqueue(new Callback<List<SubjectResponseDTO>>() {
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
}
