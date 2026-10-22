package com.iescamp.studyflow.utils;

import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.service.ApiClient;

public class UserSession {
    private static UserSession instance;
    private User loggedUser;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
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

    /**
     * Persist user and token across application restarts.
     */
    public void saveSession(User user, String token) {
        this.loggedUser = user;
        if (token != null && !token.isBlank()) {
            ApiClient.setToken(token);
            AppSettings.get().setAuthToken(token);
        }
        if (user != null) {
            try {
                String json = JsonMapper.get().writeValueAsString(user);
                AppSettings.get().setSavedUserJson(json);
                AppSettings.get().putIntPref("last.logged.userId", user.getUserId());
            } catch (Exception e) {
                System.err.println("Failed to serialize user session: " + e.getMessage());
            }
        }
    }

    /**
     * Attempts to restore the session from AppSettings.
     * Returns true if a valid user is restored.
     */
    public boolean restoreSession() {
        String token = AppSettings.get().getAuthToken();
        String userJson = AppSettings.get().getSavedUserJson();

        if (userJson.isBlank()) {
            return false;
        }

        try {
            User user = JsonMapper.get().readValue(userJson, User.class);
            if (user == null || user.getUserId() <= 0) {
                return false;
            }
            this.loggedUser = user;

            if (!token.isBlank()) {
                ApiClient.setToken(token);
            }
            return true;
        } catch (Exception e) {
            System.err.println("Failed to restore user session: " + e.getMessage());
            cleanUserSession();
            return false;
        }
    }

    public void cleanUserSession() {
        loggedUser = null;
        ApiClient.clearToken();
        AppSettings.get().clearAuthToken();
        AppSettings.get().clearSavedUserJson();
        AppSettings.get().clearTokenExpiresAt();
    }
}
