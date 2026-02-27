package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.utils.UserSession;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;

public class UserConfigController {
    @FXML private TextField txtUsername;
    @FXML private TextField txtEmail;
    @FXML private PasswordField txtNewPassword;

    @FXML
    public void initialize() {
        // Cargamos los datos actuales de la sesión
        User currentUser = UserSession.getInstance().getUser();
        if (currentUser != null) {
            txtUsername.setText(currentUser.getName());
            txtEmail.setText(currentUser.getEmail());
        }
    }

    @FXML
    private void handleUpdateUser() {
        // Aquí llamarías a tu userService.update(...)
        System.out.println("Actualizando usuario...");
    }
}