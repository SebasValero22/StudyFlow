package com.iescamp.studyflow.service;

import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.utils.JsonMapper;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.List;

public class UserService {
    // Conecta con el método register de tu API
    public void register(User user) throws Exception {
        String json = JsonMapper.get().writeValueAsString(user);
        HttpRequest request = ApiClient.prepareRequest("/users/register")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        if (ApiClient.sendRequest(request).statusCode() != 200) {
            throw new Exception("Error al registrar usuario");
        }
    }

    // Conecta con el método login de tu API
    public User login(String email, String password) throws Exception {
        String json = String.format("{\"email\":\"%s\", \"password\":\"%s\"}", email, password);
        HttpRequest request = ApiClient.prepareRequest("/users/login")
                .POST(HttpRequest.BodyPublishers.ofString(json)).build();

        HttpResponse<String> response = ApiClient.sendRequest(request);
        if (response.statusCode() == 200) {
            return JsonMapper.get().readValue(response.body(), User.class);
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