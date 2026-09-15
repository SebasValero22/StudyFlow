package com.iescamp.studyflow_api.service;

import com.iescamp.studyflow_api.dto.authDTO.AuthResponseDTO;
import com.iescamp.studyflow_api.dto.userDTO.UserRegisterDTO;
import com.iescamp.studyflow_api.dto.userDTO.UserResponseDTO;
import com.iescamp.studyflow_api.model.User;
import com.iescamp.studyflow_api.repository.UserRepository;
import com.iescamp.studyflow_api.security.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;

    @Autowired
    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtils jwtUtils) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtils = jwtUtils;
    }

    public AuthResponseDTO register(UserRegisterDTO dto) {
        if (dto.getEmail() == null || dto.getPassword() == null || dto.getName() == null) {
            throw new IllegalArgumentException("Name, email and password are required");
        }
        if (userRepository.findByEmail(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        User user = new User();
        user.setUserName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setActive(true);

        User saved = userRepository.save(user);
        return buildAuthResponse(saved);
    }

    public AuthResponseDTO login(String email, String rawPassword) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Invalid credentials"));

        verifyOrMigratePassword(user, rawPassword);
        return buildAuthResponse(user);
    }

    /**
     * BCrypt-aware verification with transparent migration:
     * - Legacy plaintext hashes are compared directly, then upgraded to BCrypt on success.
     * - BCrypt hashes are verified normally.
     */
    private void verifyOrMigratePassword(User user, String rawPassword) {
        String stored = user.getPassword();

        if (stored != null && stored.startsWith("$2")) {
            if (!passwordEncoder.matches(rawPassword, stored)) {
                throw new RuntimeException("Invalid credentials");
            }
        } else {
            if (stored == null || !stored.equals(rawPassword)) {
                throw new RuntimeException("Invalid credentials");
            }
            user.setPassword(passwordEncoder.encode(rawPassword));
            userRepository.save(user);
        }
    }

    private AuthResponseDTO buildAuthResponse(User user) {
        String token = jwtUtils.generateToken(user.getEmail());
        return new AuthResponseDTO(token, UserResponseDTO.convertToDTO(user));
    }
}
