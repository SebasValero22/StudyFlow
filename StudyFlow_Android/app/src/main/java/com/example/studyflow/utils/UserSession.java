package com.example.studyflow.utils;

import com.example.studyflow.data.model.User;

public class UserSession {
    private static UserSession instance;
    private User currentUser;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }

    public void setUser(User user) { this.currentUser = user; }
    public User getUser() { return currentUser; }
    public void logout() { currentUser = null; }
}