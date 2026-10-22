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

    @FXML private TextField txtName;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Button btnRegister;
    @FXML private Label lblStatus;

    private final UserService userService = new UserService();

    @FXML
    public void handleRegister(ActionEvent event) {
        // 1. Recogida de datos
        String name = txtName.getText().trim();
        String email = txtEmail.getText().trim();
        String password = txtPassword.getText();

        // 2. Validación local (RA4.3 - Prevención de errores)
        if (name.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showStatus("All fields are required.", Color.RED);
            return;
        }

        // Validación extra: Email formato (opcional pero recomendado)
        if (!email.contains("@")) {
            showStatus("Please enter a valid email.", Color.RED);
            return;
        }

        // 3. Preparar el objeto User (según tu modelo)
        User newUser = new User();
        newUser.setName(name);
        newUser.setEmail(email);
        newUser.setPassword(password);
        // La fecha de registro la suele poner el backend automáticamente

        // 4. Gestión Visual de Estados (RA2.3)
        btnRegister.setDisable(true); // Evitar doble clic
        showStatus("Creating account...", Color.BLUE);

        // 5. Proceso en segundo plano (PSP - Hilos)
        Task<Void> registerTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                userService.register(newUser); // Llamada a tu servicio
                return null;
            }
        };

        // ÉXITO
        registerTask.setOnSucceeded(e -> {
            btnRegister.setDisable(false);
            showStatus("Account created! Redirecting...", Color.GREEN);
            // Pequeña pausa o redirección inmediata al Login
            ViewSwitcher.loadView("login_view.fxml");
        });

        // FALLO
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