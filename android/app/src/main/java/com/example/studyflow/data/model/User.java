package com.example.studyflow.data.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.LocalDate;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {
    private Integer userId;
    private String userName;
    private String email;

    @JsonProperty("passwordHash")
    private String password;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate registrationDate;

    @JsonProperty("activeUser")
    private Boolean active;

    public User() {}

    public User(Integer userId, String userName, String email, String password, LocalDate registrationDate, Boolean active) {
        this.userId = userId;
        this.userName = userName;
        this.email = email;
        this.password = password;
        this.registrationDate = registrationDate;
        this.active = active;
    }

    // Getters y Setters
    public Integer getUserId() { return userId; }
    public void setUserId(Integer userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public Boolean getActive() { return active; }
    public void setActive(Boolean active) { this.active = active; }
}