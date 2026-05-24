package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.service.UserService;
import com.iescamp.studyflow.utils.ViewSwitcher;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;

public class RegisterController {

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnRegister;
    @FXML private Label lblStatus;

    private final UserService userService = new UserService();

    @FXML
    public void handleRegister(ActionEvent event) {
        // Recogida de datos
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // Validacion local (RA4.3 - prevencion de errores)
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("All fields are required.", Color.RED);
            return;
        }

        // Validacion extra: formato de email (opcional pero recomendado)
        if (!email.contains("@")) {
            showStatus("Please enter a valid email.", Color.RED);
            return;
        }

        // Preparar el objeto User (segun tu modelo)
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        // La fecha de registro la suele poner el backend automaticamente

        // Gestion visual de estados (RA2.3)
        btnRegister.setDisable(true); // Evitar doble clic
        showStatus("Creating account...", Color.BLUE);

        // Proceso en segundo plano (PSP - hilos)
        Task<Void> registerTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                userService.register(newUser);
                return null;
            }
        };

        // Exito
        registerTask.setOnSucceeded(e -> {
            btnRegister.setDisable(false);
            showStatus("Account created! Redirecting...", Color.GREEN);
            // Pequena pausa o redireccion inmediata al login
            ViewSwitcher.loadView("login_view.fxml");
        });

        // Fallo
        registerTask.setOnFailed(e -> {
            btnRegister.setDisable(false);
            Throwable error = registerTask.getException();
            showStatus("Error: " + error.getMessage(), Color.RED);
        });

        new Thread(registerTask).start();
    }

    @FXML
    public void handleBackToLogin(ActionEvent event) {
        ViewSwitcher.loadView("login_view.fxml");
    }

    private void showStatus(String message, Color color) {
        lblStatus.setTextFill(color);
        lblStatus.setText(message);
    }
}
