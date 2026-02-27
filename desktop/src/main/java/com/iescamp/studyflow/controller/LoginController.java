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

    @FXML public TextField txtUsername;
    @FXML public PasswordField txtPassword;
    @FXML public Button btnLogin;
    @FXML public Button btnRegister;
    @FXML public Label lblStatus;

    private final UserService userService = new UserService();

    @FXML
    public void handleLogin(ActionEvent actionEvent) {
        String email = txtUsername.getText();
        String password = txtPassword.getText();

        // 1. Validación
        if (email.isEmpty() || password.isEmpty()) {
            lblStatus.setTextFill(Color.RED);
            lblStatus.setText("Por favor, rellena todos los campos.");
            return;
        }

        // 2. Feedback visual
        lblStatus.setTextFill(Color.BLUE);
        lblStatus.setText("Conectando con el servidor...");
        btnLogin.setDisable(true);

        // 3. Tarea en segundo plano
        Task<User> loginTask = new Task<>() {
            @Override
            protected User call() throws Exception {
                return userService.login(email, password);
            }
        };

        // 4. ÉXITO
        loginTask.setOnSucceeded(e -> {
            User user = loginTask.getValue();

            // --- AQUÍ ESTÁ EL CAMBIO CLAVE ---
            // Guardamos el usuario en la sesión global
            UserSession.getInstance().setUser(user);
            System.out.println("Sesión iniciada para ID: " + user.getUserId());
            // ---------------------------------

            // Navegar al Dashboard
            ViewSwitcher.loadView("main_dashboard.fxml");
        });

        // 5. ERROR
        loginTask.setOnFailed(e -> {
            Throwable error = loginTask.getException();
            btnLogin.setDisable(false);
            lblStatus.setText("Error de acceso");

            Alert alert = new Alert(Alert.AlertType.ERROR);
            alert.setTitle("Login Error");
            alert.setHeaderText("No se pudo iniciar sesión");
            alert.setContentText(error.getMessage());
            alert.showAndWait();
        });

        new Thread(loginTask).start();
    }

    @FXML
    public void handleRegister(ActionEvent actionEvent) {
            try {
                // Verifica que la ruta coincida con tu estructura de carpetas
                ViewSwitcher.loadView("register_view.fxml");
            } catch (Exception e) {
                System.err.println("Error al cargar la vista de registro: " + e.getMessage());
                e.printStackTrace();
            }
        }
}