package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.enums.Priority;
import com.iescamp.studyflow.model.Subject;
import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.service.SubjectService;
import com.iescamp.studyflow.service.TaskService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.time.LocalDate;
import java.util.List;

public class TaskFormController {

    @FXML public TextField titleField;
    @FXML public TextArea descArea;
    @FXML public ComboBox<Subject> subjectCombo;
    @FXML public DatePicker dueDatePicker;
    @FXML public ComboBox<Priority> priorityCombo;
    @FXML public CheckBox completedCheckBox;
    @FXML public Label formStatusLabel;

    private final TaskService taskService = new TaskService();
    private final SubjectService subjectService = new SubjectService();
    private Task existingTask = null;

    @FXML
    public void initialize() {
        priorityCombo.setItems(FXCollections.observableArrayList(Priority.values()));
        loadSubjects();
    }

    private void loadSubjects() {
        try {
            List<Subject> subjects = subjectService.getAllSubjects();
            subjectCombo.setItems(FXCollections.observableArrayList(subjects));
            subjectCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Subject s) { return (s != null) ? s.getNameSubject() : ""; }
                @Override
                public Subject fromString(String string) { return null; }
            });
        } catch (Exception e) {
            formStatusLabel.setText("Error loading subjects.");
        }
    }

    public void setTask(Task task) {
        this.existingTask = task;
        titleField.setText(task.getTitle());
        descArea.setText(task.getDescriptionTask());
        dueDatePicker.setValue(task.getDueDate());
        completedCheckBox.setSelected(task.getIsCompleted() != null && task.getIsCompleted());
        
        if (task.getPriority() != null) {
            try {
                priorityCombo.setValue(Priority.valueOf(task.getPriority().toUpperCase()));
            } catch (Exception e) {}
        }

        if (task.getSubjectId() != null) {
            subjectCombo.getItems().stream()
                .filter(s -> s.getSubjectId() == task.getSubjectId())
                .findFirst()
                .ifPresent(s -> subjectCombo.setValue(s));
        }
    }

    public void handleSave(ActionEvent actionEvent) {
        if (titleField.getText().isEmpty() || subjectCombo.getValue() == null) {
            formStatusLabel.setText("Title and Subject are required.");
            formStatusLabel.setTextFill(Color.RED);
            return;
        }

        try {
            Task task = (existingTask != null) ? existingTask : new Task();
            task.setTitle(titleField.getText());
            task.setDescriptionTask(descArea.getText());
            task.setSubjectId(subjectCombo.getValue().getSubjectId());
            task.setIsCompleted(completedCheckBox.isSelected());
            task.setDueDate(dueDatePicker.getValue());

            if (priorityCombo.getValue() != null) {
                task.setPriority(priorityCombo.getValue().toString());
            }

            if (existingTask != null) {
                taskService.updateTask(task.getTaskId(), task);
            } else {
                taskService.saveTask(task);
            }

            closeWindow();
        } catch (Exception e) {
            formStatusLabel.setText("Error: " + e.getMessage());
            formStatusLabel.setTextFill(Color.RED);
        }
    }

    public void handleCancel(ActionEvent actionEvent) {
        closeWindow();
    }

    private void closeWindow() {
        Stage stage = (Stage) titleField.getScene().getWindow();
        stage.close();
    }
}
