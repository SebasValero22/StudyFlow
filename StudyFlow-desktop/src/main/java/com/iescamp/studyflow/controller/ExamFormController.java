package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Exam;
import com.iescamp.studyflow.model.Subject;
import com.iescamp.studyflow.service.ExamService;
import com.iescamp.studyflow.service.SubjectService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;

public class ExamFormController {
    @FXML private ComboBox<Subject> subjectCombo;
    @FXML private TextField nameField, typeField, roomField;
    @FXML private DatePicker datePicker;
    @FXML private Label statusLabel;

    private final ExamService examService = new ExamService();
    private final SubjectService subjectService = new SubjectService();
    private Exam existingExam = null;

    @FXML
    public void initialize() {
        try {
            subjectCombo.setItems(FXCollections.observableArrayList(subjectService.getAllSubjects()));
            subjectCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Subject s) { return (s != null) ? s.getNameSubject() : ""; }
                @Override
                public Subject fromString(String string) { return null; }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setExam(Exam exam) {
        this.existingExam = exam;
        nameField.setText(exam.getNameExam());
        typeField.setText(exam.getExamType());
        datePicker.setValue(exam.getExamDate());
        roomField.setText(exam.getClassroom());
        
        subjectCombo.getItems().stream()
            .filter(s -> s.getSubjectId() == exam.getSubjectId())
            .findFirst()
            .ifPresent(s -> subjectCombo.setValue(s));
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || subjectCombo.getValue() == null || datePicker.getValue() == null) {
            statusLabel.setText("Required fields missing.");
            return;
        }

        try {
            Exam exam = (existingExam != null) ? existingExam : new Exam();
            exam.setNameExam(nameField.getText());
            exam.setExamType(typeField.getText());
            exam.setExamDate(datePicker.getValue());
            exam.setClassroom(roomField.getText());
            exam.setSubjectId(subjectCombo.getValue().getSubjectId());

            if (existingExam != null) {
                examService.updateExam(exam.getExamId(), exam);
            } else {
                examService.saveExam(exam);
            }
            handleCancel();
        } catch (Exception e) {
            statusLabel.setText("Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}
