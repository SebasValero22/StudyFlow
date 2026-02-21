package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.service.UserService;
import com.iescamp.studyflow.utils.UserSession; // Importante
import com.iescamp.studyflow.utils.ViewSwitcher;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;

public class LoginController {

    @FXML public TextField emailField;
    @FXML public PasswordField passwordField;
    @FXML public Label messageLabel;
    @FXML public Button btnLogin;

    private final UserService userService = new UserService();

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String email = emailField.getText();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            messageLabel.setText("Please fill all fields.");
            return;
        }

        messageLabel.setText("Connecting...");
        // Use btnLogin if you want to disable it, but need @FXML for it
        
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return userService.login(email, password);
            }
        };

        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();
            UserSession.getInstance().setUser(user);
            ViewSwitcher.loadView("main_dashboard.fxml");
        });

        loginTask.setOnFailed(e -> {
            messageLabel.setText("Login failed: " + loginTask.getException().getMessage());
        });

        new Thread(loginTask).start();
    }

    @FXML
    public void goToRegister(ActionEvent actionEvent) {
        ViewSwitcher.loadView("register_view.fxml");
    }
}