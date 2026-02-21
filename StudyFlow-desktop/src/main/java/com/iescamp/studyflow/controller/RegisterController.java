package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.service.UserService;
import com.iescamp.studyflow.utils.ViewSwitcher; // Asumiendo que tienes tu gestor de vistas
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class RegisterController {

    @FXML private TextField nameField;
    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;
    @FXML private Button btnRegister;
    @FXML private Label messageLabel;

    private final UserService userService = new UserService();

    @FXML
    public void handleRegister(ActionEvent event) {
        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("All fields are required.");
            return;
        }

        messageLabel.setText("Creating account...");

        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);

        Task<Void> registerTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                userService.register(newUser);
                return null;
            }
        };

        registerTask.setOnSucceeded(e -> {
            ViewSwitcher.loadView("login_view.fxml");
        });

        registerTask.setOnFailed(e -> {
            messageLabel.setText("Error: " + registerTask.getException().getMessage());
        });

        new Thread(registerTask).start();
    }

    @FXML
    public void goToLogin(ActionEvent event) {
        ViewSwitcher.loadView("login_view.fxml");
    }
}