package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.authDTO.AuthResponseDTO;
import com.iescamp.studyflow_api.dto.userDTO.UserLoginDTO;
import com.iescamp.studyflow_api.dto.userDTO.UserRegisterDTO;
import com.iescamp.studyflow_api.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody UserRegisterDTO dto) {
        try {
            return ResponseEntity.ok(authService.register(dto));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Registration failed: " + e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody UserLoginDTO dto) {
        try {
            return ResponseEntity.ok(authService.login(dto.getEmail(), dto.getPassword()));
        } catch (RuntimeException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body("Login failed: " + e.getMessage());
        }
    }
}
