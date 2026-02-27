package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Subject;
import com.iescamp.studyflow.model.User;
import com.iescamp.studyflow.service.SubjectService;
import com.iescamp.studyflow.utils.UserSession; // Importante
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.time.LocalDate;

public class SubjectFormController {

    @FXML private TextField nameField;
    @FXML private DatePicker academicYearPicker;
    @FXML private ColorPicker colorPicker;
    @FXML private Label formStatusLabel;

    private final SubjectService subjectService = new SubjectService();

    @FXML
    public void initialize() {
        colorPicker.setValue(Color.web("#3498db"));
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (nameField.getText().trim().isEmpty()) {
            formStatusLabel.setText("El nombre es obligatorio");
            formStatusLabel.setTextFill(Color.RED);
            return;
        }

        try {
            // 1. Obtener usuario de la sesión
            User currentUser = UserSession.getInstance().getUser();

            // Verificación de seguridad (por si se perdió la sesión)
            if (currentUser == null) {
                formStatusLabel.setText("Error: No hay sesión activa. Relogueate.");
                formStatusLabel.setTextFill(Color.RED);
                return;
            }

            // 2. Crear objeto Subject
            Subject subject = new Subject();
            subject.setNameSubject(nameField.getText());

            // --- AQUÍ ESTÁ EL CAMBIO CLAVE ---
            // Usamos el ID real del usuario logueado
            subject.setUserId(currentUser.getUserId());
            // ---------------------------------

            // Fechas
            LocalDate date = academicYearPicker.getValue();
            if (date != null) {
                subject.setAcademicYear(date.toString());
            } else {
                subject.setAcademicYear("");
            }

            // Color
            String hexColor = toHexString(colorPicker.getValue());
            subject.setColor(hexColor);

            // Active (Por defecto true al crear)
            subject.setActiveSubject(true);

            // 3. Guardar
            subjectService.saveSubject(subject);

            // 4. Cerrar
            closeWindow();

        } catch (Exception e) {
            e.printStackTrace();
            formStatusLabel.setText("Error al guardar: " + e.getMessage());
            formStatusLabel.setTextFill(Color.RED);
        }
    }

    @FXML
    public void handleCancel(ActionEvent event) {
        closeWindow();
    }

    @FXML
    public void handleDelete(ActionEvent event) {
        // Opcional
    }

    private void closeWindow() {
        Stage stage = (Stage) nameField.getScene().getWindow();
        stage.close();
    }

    private String toHexString(Color color) {
        return String.format("#%02X%02X%02X",
                (int) (color.getRed() * 255),
                (int) (color.getGreen() * 255),
                (int) (color.getBlue() * 255));
    }
}