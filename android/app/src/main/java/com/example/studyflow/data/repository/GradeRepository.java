package com.example.studyflow.data.repository;

import com.example.studyflow.data.dto.GradeResponseDTO;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GradeRepository {
    private ApiService apiService;

    public GradeRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void getGrades(TaskRepository.RepositoryCallback<List<GradeResponseDTO>> callback) {
        apiService.getGrades().enqueue(new Callback<List<GradeResponseDTO>>() {
            @Override
            public void onResponse(Call<List<GradeResponseDTO>> call, Response<List<GradeResponseDTO>> response) {
                if (response.isSuccessful()) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error fetching grades");
                }
            }

            @Override
            public void onFailure(Call<List<GradeResponseDTO>> call, Throwable t) {
                callback.onError(t.getMessage());
            }
        });
    }
}
