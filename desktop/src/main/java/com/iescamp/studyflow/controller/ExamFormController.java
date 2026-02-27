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
    @FXML private Button saveButton;

    private final ExamService examService = new ExamService();
    private final SubjectService subjectService = new SubjectService();
    private Exam examToEdit;

    @FXML
    public void initialize() {
        try {
            subjectCombo.setItems(FXCollections.observableArrayList(subjectService.getAllSubjects()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setExamToEdit(Exam exam) {
        this.examToEdit = exam;
        nameField.setText(exam.getNameExam());
        typeField.setText(exam.getExamType());
        roomField.setText(exam.getClassroom());
        datePicker.setValue(exam.getExamDate());
        
        if (exam.getSubjectId() != 0) {
            for (Subject s : subjectCombo.getItems()) {
                if (s.getSubjectId() == exam.getSubjectId()) {
                    subjectCombo.setValue(s);
                    break;
                }
            }
        }
        saveButton.setText("Update");
    }

    @FXML
    private void handleSave() {
        if (nameField.getText().isEmpty() || subjectCombo.getValue() == null || datePicker.getValue() == null) {
            statusLabel.setText("Please fill in the required fields.");
            return;
        }

        try {
            Exam exam = (examToEdit != null) ? examToEdit : new Exam();
            exam.setNameExam(nameField.getText());
            exam.setExamType(typeField.getText());
            exam.setExamDate(datePicker.getValue());
            exam.setClassroom(roomField.getText());
            exam.setSubjectId(subjectCombo.getValue().getSubjectId());

            if (examToEdit != null) {
                examService.updateExam(exam.getExamId(), exam);
            } else {
                examService.saveExam(exam);
            }
            handleCancel();
        } catch (Exception e) {
            statusLabel.setText("Error saving: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        ((Stage) nameField.getScene().getWindow()).close();
    }
}