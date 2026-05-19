package com.iescamp.studyflow_api.service;


import com.iescamp.studyflow_api.dto.userDTO.UserRegisterDTO;
import com.iescamp.studyflow_api.dto.userDTO.UserResponseDTO;
import com.iescamp.studyflow_api.model.User;
import com.iescamp.studyflow_api.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    public UserResponseDTO register(UserRegisterDTO dto) {
        // Convertimos el DTO a entidad
        User user = new User();
        user.setUserName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(dto.getPassword()); // En un entorno real, aqui se encriptaria

        User savedUser = userRepository.save(user);
        return UserResponseDTO.convertToDTO(savedUser);
    }

    public UserResponseDTO login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Contraseña incorrecta");
        }
        return UserResponseDTO.convertToDTO(user);
    }

    @Transactional
    public void delete(String email) {
        userRepository.deleteByEmail(email);
    }

    public UserResponseDTO findByEmail(String email) {
        // Buscamos; si no existe, lanzamos error
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Usuario no encontrado con email: " + email));
        // Convertimos a DTO
        return UserResponseDTO.convertToDTO(user);
    }

    public UserResponseDTO modify(String email, UserRegisterDTO dto) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found with email: " + email));

        user.setUserName(dto.getName());
        // user.setPassword(dto.getPassword()); // Opcional: actualizar contrasena si hace falta

        User savedUser = userRepository.save(user);

        return UserResponseDTO.convertToDTO(savedUser);
    }
}
