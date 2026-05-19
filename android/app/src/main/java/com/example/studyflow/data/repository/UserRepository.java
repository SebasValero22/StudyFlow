package com.example.studyflow.data.repository;

import com.example.studyflow.data.dto.UserLoginDTO;
import com.example.studyflow.data.dto.UserRegisterDTO;
import com.example.studyflow.data.dto.UserResponseDTO;
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
        UserLoginDTO loginData = new UserLoginDTO(email, password);

        apiService.login(loginData).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Invalid credentials");
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                callback.onError("Connection error: " + t.getMessage());
            }
        });
    }

    public void registerUser(String name, String email, String password, LoginCallback callback) {
        UserRegisterDTO registerData = new UserRegisterDTO(name, email, password);
        apiService.register(registerData).enqueue(new Callback<UserResponseDTO>() {
            @Override
            public void onResponse(Call<UserResponseDTO> call, Response<UserResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null) {
                    callback.onSuccess(response.body());
                } else {
                    callback.onError("Registration error");
                }
            }

            @Override
            public void onFailure(Call<UserResponseDTO> call, Throwable t) {
                callback.onError("Connection error: " + t.getMessage());
            }
        });
    }

    public interface LoginCallback {
        void onSuccess(UserResponseDTO user);
        void onError(String message);
    }
}
