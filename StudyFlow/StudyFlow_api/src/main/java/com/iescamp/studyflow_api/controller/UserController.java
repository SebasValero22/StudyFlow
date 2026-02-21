package com.iescamp.studyflow_api.controller;

import com.iescamp.studyflow_api.dto.userDTO.UserLoginDTO;
import com.iescamp.studyflow_api.dto.userDTO.UserRegisterDTO;
import com.iescamp.studyflow_api.dto.userDTO.UserResponseDTO;
import com.iescamp.studyflow_api.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@CrossOrigin(origins = "*") // <-- THIS IS MANDATORY FOR THE PWA
@RestController
@RequestMapping("/api/users")
public class UserController {

        @Autowired
        private UserService userService;

        @PostMapping("/register")
        public UserResponseDTO register(@RequestBody UserRegisterDTO registro) {
            return userService.register(registro);
        }

        @PostMapping("/login")
        public UserResponseDTO login(@RequestBody UserLoginDTO login) {
            return userService.login(login.getEmail(), login.getPassword());
        }

        @GetMapping("/search")
        public UserResponseDTO findByEmail(@RequestParam String email) {
            return userService.findByEmail(email);
        }

//        @GetMapping("/all")
//        public List<UserResponseDTO> getAll() {
//            return userService.findAll();
//        }
}
