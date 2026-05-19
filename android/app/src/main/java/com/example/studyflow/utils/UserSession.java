package com.example.studyflow.utils;

import com.example.studyflow.data.dto.UserResponseDTO;

public class UserSession {
    private static UserSession instance;
    private UserResponseDTO currentUser;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(UserResponseDTO user) { this.currentUser = user; }
    public UserResponseDTO getUser() { return currentUser; }
    public boolean isLoggedIn() { return currentUser != null; }
    public void logout() { currentUser = null; }
}
