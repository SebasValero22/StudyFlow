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
    private Subject existingSubject = null;

    @FXML
    public void initialize() {
        colorPicker.setValue(Color.web("#3498db"));
    }

    public void setSubject(Subject subject) {
        this.existingSubject = subject;
        nameField.setText(subject.getNameSubject());
        if (subject.getAcademicYear() != null && !subject.getAcademicYear().isEmpty()) {
            try {
                academicYearPicker.setValue(LocalDate.parse(subject.getAcademicYear()));
            } catch (Exception e) {}
        }
        if (subject.getColor() != null) {
            colorPicker.setValue(Color.web(subject.getColor()));
        }
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
                formStatusLabel.setText("No active session.");
                return;
            }

            Subject subject = (existingSubject != null) ? existingSubject : new Subject();
            subject.setNameSubject(nameField.getText());
            subject.setUserId(currentUser.getUserId());

            LocalDate date = academicYearPicker.getValue();
            subject.setAcademicYear(date != null ? date.toString() : "");
            subject.setColor(toHexString(colorPicker.getValue()));
            subject.setActiveSubject(true);

            if (existingSubject != null) {
                subjectService.updateSubject(subject.getSubjectId(), subject);
            } else {
                subjectService.saveSubject(subject);
            }

            closeWindow();
        } catch (Exception e) {
            e.printStackTrace();
            formStatusLabel.setText("Error saving: " + e.getMessage());
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