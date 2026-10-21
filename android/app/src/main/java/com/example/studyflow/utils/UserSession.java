package com.example.studyflow.utils;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.studyflow.data.dto.UserResponseDTO;

/**
 * Session store: JWT + user, persisted in SharedPreferences so a cold start
 * keeps the login. Call {@link #init(Context)} once from the Application class
 * before using any other method.
 */
public class UserSession {
    private static final String PREFS = "studyflow_session";
    private static final String KEY_TOKEN = "token";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_NAME = "name";
    private static final String KEY_EMAIL = "email";

    private static UserSession instance;

    private SharedPreferences prefs;
    private String token;
    private UserResponseDTO currentUser;

    private UserSession() {}

    public static synchronized UserSession getInstance() {
        if (instance == null) { instance = new UserSession(); }
        return instance;
    }

    /** Must be called once at app startup (Application.onCreate). */
    public synchronized void init(Context context) {
        if (prefs != null) return;
        prefs = context.getApplicationContext()
                .getSharedPreferences(PREFS, Context.MODE_PRIVATE);
        token = prefs.getString(KEY_TOKEN, null);
        Integer userId = prefs.getInt(KEY_USER_ID, -1);
        if (token != null && userId > 0) {
            currentUser = new UserResponseDTO();
            currentUser.setUserId(userId);
            currentUser.setName(prefs.getString(KEY_NAME, null));
            currentUser.setEmail(prefs.getString(KEY_EMAIL, null));
        }
    }

    /** Store a fresh login: memory + disk. */
    public synchronized void setSession(String newToken, UserResponseDTO user) {
        this.token = newToken;
        this.currentUser = user;
        if (prefs != null && user != null) {
            prefs.edit()
                    .putString(KEY_TOKEN, newToken)
                    .putInt(KEY_USER_ID, user.getUserId() != null ? user.getUserId() : -1)
                    .putString(KEY_NAME, user.getName())
                    .putString(KEY_EMAIL, user.getEmail())
                    .apply();
        }
    }

    /** Legacy setter kept for compatibility; prefer {@link #setSession}. */
    public void setUser(UserResponseDTO user) { this.currentUser = user; }

    public synchronized String getToken() { return token; }

    public UserResponseDTO getUser() { return currentUser; }

    public synchronized boolean isLoggedIn() { return token != null && currentUser != null; }

    public synchronized void logout() {
        token = null;
        currentUser = null;
        if (prefs != null) {
            prefs.edit().clear().apply();
        }
    }
}
