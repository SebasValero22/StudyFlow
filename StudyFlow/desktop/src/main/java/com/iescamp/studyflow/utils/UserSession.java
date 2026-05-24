package com.iescamp.studyflow.utils;

import com.iescamp.studyflow.model.User;

public class UserSession {
    // Instancia unica (singleton)
    private static UserSession instance;
    private User loggedUser; // Aqui guardamos al usuario conectado
    private UserSession() {}
    public static UserSession getInstance() {
        if (instance == null) {
            instance = new UserSession();
        }
        return instance;
    }
    public void setUser(User user) {
        this.loggedUser = user;
    }
    public User getUser() {
        return loggedUser;
    }
    public void cleanUserSession() {
        loggedUser = null; // Para cuando hagas logout
    }
}
