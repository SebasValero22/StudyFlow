package com.example.studyflow.data.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/** Response of POST /api/auth/login and /api/auth/register: { token, user }. */
@JsonIgnoreProperties(ignoreUnknown = true)
public class AuthResponseDTO {
    private String token;
    private UserResponseDTO user;

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public UserResponseDTO getUser() { return user; }
    public void setUser(UserResponseDTO user) { this.user = user; }
}
