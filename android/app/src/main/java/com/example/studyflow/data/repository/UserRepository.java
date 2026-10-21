package com.example.studyflow.data.repository;

import com.example.studyflow.data.dto.AuthResponseDTO;
import com.example.studyflow.data.dto.UserLoginDTO;
import com.example.studyflow.data.dto.UserRegisterDTO;
import com.example.studyflow.data.dto.UserResponseDTO;
import com.example.studyflow.data.remote.ApiClient;
import com.example.studyflow.data.remote.ApiService;
import com.example.studyflow.utils.UserSession;
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
        apiService.login(loginData).enqueue(new Callback<AuthResponseDTO>() {
            @Override
            public void onResponse(Call<AuthResponseDTO> call, Response<AuthResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getToken() != null) {
                    AuthResponseDTO auth = response.body();
                    UserSession.getInstance().setSession(auth.getToken(), auth.getUser());
                    callback.onSuccess(auth.getUser());
                } else {
                    callback.onError(errorMessage(response, "Login failed"));
                }
            }

            @Override
            public void onFailure(Call<AuthResponseDTO> call, Throwable t) {
                callback.onError("Connection error: " + t.getMessage());
            }
        });
    }

    public void registerUser(String name, String email, String password, LoginCallback callback) {
        UserRegisterDTO registerData = new UserRegisterDTO(name, email, password);
        apiService.register(registerData).enqueue(new Callback<AuthResponseDTO>() {
            @Override
            public void onResponse(Call<AuthResponseDTO> call, Response<AuthResponseDTO> response) {
                if (response.isSuccessful() && response.body() != null
                        && response.body().getToken() != null) {
                    AuthResponseDTO auth = response.body();
                    UserSession.getInstance().setSession(auth.getToken(), auth.getUser());
                    callback.onSuccess(auth.getUser());
                } else {
                    callback.onError(errorMessage(response, "Registration error"));
                }
            }

            @Override
            public void onFailure(Call<AuthResponseDTO> call, Throwable t) {
                callback.onError("Connection error: " + t.getMessage());
            }
        });
    }

    /** 401/400 responses carry a plain-text reason from the API. */
    private static String errorMessage(Response<?> response, String fallback) {
        try {
            if (response.errorBody() != null) {
                String body = response.errorBody().string();
                if (body != null && !body.trim().isEmpty()) {
                    return body.trim();
                }
            }
        } catch (Exception ignored) {
            // fall through to fallback
        }
        return fallback + " (HTTP " + response.code() + ")";
    }

    public interface LoginCallback {
        void onSuccess(UserResponseDTO user);
        void onError(String message);
    }
}
