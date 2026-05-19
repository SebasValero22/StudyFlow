package com.example.studyflow.data.dto;

public class UserRegisterDTO {
    private String name;
    private String email;
    private String password;

    public UserRegisterDTO() {}
    public UserRegisterDTO(String name, String email, String password) {
        this.name = name;
        this.email = email;
        this.password = password;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
