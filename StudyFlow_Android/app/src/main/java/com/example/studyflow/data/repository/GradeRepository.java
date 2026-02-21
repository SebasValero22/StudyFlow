package com.example.studyflow.data.repository;

import com.example.studyflow.data.model.Grade;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback; // Importar retrofit2.Callback
import retrofit2.Response;

public class GradeRepository {
    private final ApiService apiService;

    public GradeRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchGrades(Callback callback) {
        apiService.getGrades().enqueue(new retrofit2.Callback<List<Grade>>() {
            @Override
            public void onResponse(Call<List<Grade>> call, Response<List<Grade>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al cargar notas: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Grade>> call, Throwable t) {
                callback.onError("Fallo de conexión: " + t.getMessage());
            }
        });
    }

    public interface Callback {
        void onSuccess(List<Grade> grades);
        void onError(String message);
    }
}