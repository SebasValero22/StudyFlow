package com.iescamp.studyflow_api.dto.userDTO;

import com.iescamp.studyflow_api.model.User;
import lombok.Data;

    @Data
    public class UserResponseDTO {
        private Integer userId;
        private String name;
        private String email;

        public static UserResponseDTO convertToDTO(User entity) {
            UserResponseDTO dto = new UserResponseDTO();
            dto.setUserId(entity.getUserId());
            dto.setName(entity.getUserName());
            dto.setEmail(entity.getEmail());
            return dto;
        }
    }
