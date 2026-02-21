package com.example.studyflow.data.repository;

import com.example.studyflow.data.model.Subject;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SubjectRepository {
    private final ApiService apiService;

    public SubjectRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void fetchSubjects(Callback callback) {
        apiService.getSubjects().enqueue(new retrofit2.Callback<List<Subject>>() {
            @Override
            public void onResponse(Call<List<Subject>> call, Response<List<Subject>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Error al cargar asignaturas: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<List<Subject>> call, Throwable t) {
                callback.onError("Fallo de conexión: " + t.getMessage());
            }
        });
    }

    // Interfaz para comunicar resultados al ViewModel
    public interface Callback {
        void onSuccess(List<Subject> subjects);
        void onError(String message);
    }
}