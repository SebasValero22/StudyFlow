package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.model.Subject;
import com.iescamp.studyflow.service.ExamService;
import com.iescamp.studyflow.service.SubjectService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class ExamFormController {
    @FXML private ComboBox<Subject> subjectCombo;
    @FXML private TextField nameField, typeField, roomField;
    @FXML private DatePicker datePicker;
    @FXML private Label statusLabel;

    private final ExamService examService = new ExamService();
    private final SubjectService subjectService = new SubjectService();

    @FXML
    public void initialize() {
        try {
            subjectCombo.setItems(FXCollections.observableArrayList(subjectService.getAllSubjects()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || subjectCombo.getValue() == null || datePicker.getValue() == null) {
            statusLabel.setText("Por favor, rellena los campos obligatorios.");
            return;
        }

        try {
            Exam newExam = new Exam();
            newExam.setNameExam(nameField.getText());
            newExam.setExamType(typeField.getText());
            newExam.setExamDate(datePicker.getValue());
            newExam.setClassroom(roomField.getText());
            newExam.setSubjectId(subjectCombo.getValue().getSubjectId());

            examService.saveExam(newExam); // Este método debe llamar al POST de la API
            handleCancel();
        } catch (Exception e) {
            statusLabel.setText("Error al guardar: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}