package com.iescamp.studyflow_api.dto.userDTO;
import lombok.Data;

@Data
public class UserRegisterDTO {
    private String name;
    private String email;
    private String password;
}