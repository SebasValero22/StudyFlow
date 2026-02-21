package com.example.studyflow.data.repository;

import com.example.studyflow.data.model.User;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class UserRepository {
    private ApiService apiService;

    public UserRepository() {
        this.apiService = ApiClient.getClient().create(ApiService.class);
    }

    public void loginUser(String email, String password, LoginCallback callback) {
        // Creamos un objeto User temporal para el login
        User loginData = new User();
        loginData.setEmail(email);
        loginData.setPassword(password);

        apiService.login(loginData).enqueue(new Callback<User>() {
            @Override
            public void onResponse(Call<User> call, Response<User> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Credenciales incorrectas");
                }
            }

            @Override
            public void onFailure(Call<User> call, Throwable t) {
                callback.onError("Error de conexión: " + t.getMessage());
            }
        });
    }

    public interface LoginCallback {
        void onSuccess(User user);
        void onError(String message);
    }
}