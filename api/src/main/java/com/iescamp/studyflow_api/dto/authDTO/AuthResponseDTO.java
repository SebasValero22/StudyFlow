package com.iescamp.studyflow_api.dto.authDTO;

import com.iescamp.studyflow_api.dto.userDTO.UserResponseDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private UserResponseDTO user;
}
