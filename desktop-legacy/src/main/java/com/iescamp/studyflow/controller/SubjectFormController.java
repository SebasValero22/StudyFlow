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
    @FXML private Button saveButton;

    private final SubjectService subjectService = new SubjectService();
    private Subject subjectToEdit;

    @FXML
    public void initialize() {
        colorPicker.setValue(Color.web("#3498db"));
    }

    public void setSubjectToEdit(Subject subject) {
        this.subjectToEdit = subject;
        nameField.setText(subject.getNameSubject());
        if (subject.getAcademicYear() != null && !subject.getAcademicYear().isEmpty()) {
            academicYearPicker.setValue(LocalDate.parse(subject.getAcademicYear()));
        }
        if (subject.getColor() != null && !subject.getColor().isEmpty()) {
            colorPicker.setValue(Color.web(subject.getColor()));
        }
        saveButton.setText("Update");
    }

    @FXML
    public void handleSave(ActionEvent event) {
        if (nameField.getText().trim().isEmpty()) {
            formStatusLabel.setText("Name is required");
            formStatusLabel.setTextFill(Color.RED);
            return;
        }

        try {
            User currentUser = UserSession.getInstance().getUser();
            if (currentUser == null) {
                formStatusLabel.setText("Error: No active session. Please login again.");
                formStatusLabel.setTextFill(Color.RED);
                return;
            }

            Subject subject = (subjectToEdit != null) ? subjectToEdit : new Subject();
            subject.setNameSubject(nameField.getText());
            subject.setUserId(currentUser.getUserId());

            LocalDate date = academicYearPicker.getValue();
            subject.setAcademicYear(date != null ? date.toString() : "");

            String hexColor = toHexString(colorPicker.getValue());
            subject.setColor(hexColor);
            subject.setActiveSubject(true);

            if (subjectToEdit != null) {
                subjectService.updateSubject(subject.getSubjectId(), subject);
            } else {
                subjectService.saveSubject(subject);
            }

            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            formStatusLabel.setText("Error saving: " + e.getMessage());
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