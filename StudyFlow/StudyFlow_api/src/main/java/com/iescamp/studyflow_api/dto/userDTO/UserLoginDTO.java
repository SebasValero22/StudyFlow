package com.iescamp.studyflow_api.dto.userDTO;

import lombok.Data;

@Data
public class UserLoginDTO {
    private String email;
    private String password;
}