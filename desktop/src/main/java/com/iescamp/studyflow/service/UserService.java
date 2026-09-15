package com.iescamp.studyflow.service;

import com.iescamp.studyflow.model.AuthResponse;
import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.utils.JsonMapper;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    // Conecta con el método register de tu API (/auth/register)
    public void register(User user) throws Exception {
        String json = String.format(
                "{\"name\":%s,\"email\":%s,\"password\":%s}",
                JsonMapper.get().writeValueAsString(user.getName()),
                JsonMapper.get().writeValueAsString(user.getEmail()),
                JsonMapper.get().writeValueAsString(user.getPassword()));

        HttpRequest request = ApiClient.prepareRequest("/auth/register")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = ApiClient.sendRequest(request);
        if (response.statusCode() != 200) {
            throw new Exception("Error al registrar usuario");
        }

        // Registration also issues a session token -> store it
        AuthResponse auth = JsonMapper.get().readValue(response.body(), AuthResponse.class);
        ApiClient.setToken(auth.getToken());
    }

    // Conecta con el método login de tu API (/auth/login)
    public User login(String email, String password) throws Exception {
        String json = String.format(
                "{\"email\":%s,\"password\":%s}",
                JsonMapper.get().writeValueAsString(email),
                JsonMapper.get().writeValueAsString(password));

        HttpRequest request = ApiClient.prepareRequest("/auth/login")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = ApiClient.sendRequest(request);
        if (response.statusCode() == 200) {
            AuthResponse auth = JsonMapper.get().readValue(response.body(), AuthResponse.class);
            ApiClient.setToken(auth.getToken());
            return auth.getUser();
        }
        throw new Exception("Login fallido");
    }


    // get all
    public List getAllUsers() throws Exception {
        List<User> users = new ArrayList<>();
        HttpRequest request = ApiClient.prepareRequest("/users/getAllUsers").build();
        HttpResponse<String> response = ApiClient.sendRequest(request);
        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), List.class);
        }
        throw new Exception("Lista no encontrada");
    }
}
