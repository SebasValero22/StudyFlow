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

import java.sql.Date;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;

public class TaskFormController {

    @FXML public TextField titleField;
    @FXML public TextArea descArea;
    @FXML public ComboBox<Subject> subjectCombo;
    @FXML public DatePicker dueDatePicker;
    @FXML public ComboBox<Priority> priorityCombo;
    @FXML public CheckBox completedCheckBox;
    @FXML public Label formStatusLabel;
    @FXML private Button saveButton;

    private final TaskService taskService = new TaskService();
    private final SubjectService subjectService = new SubjectService();
    private Task taskToEdit;

    @FXML
    public void initialize() {
        // 1. Cargar Prioridades
        priorityCombo.setItems(FXCollections.observableArrayList(Priority.values()));

        // 2. Cargar Asignaturas
        loadSubjects();
    }

    public void setTaskToEdit(Task task) {
        this.taskToEdit = task;
        titleField.setText(task.getTitle());
        descArea.setText(task.getDescriptionTask());
        dueDatePicker.setValue(task.getDueDate());
        completedCheckBox.setSelected(task.getIsCompleted() != null && task.getIsCompleted());
        if (task.getPriority() != null) {
            priorityCombo.setValue(Priority.valueOf(task.getPriority().toUpperCase()));
        }
        
        // Seleccionar la asignatura en el combo
        if (task.getSubjectId() != null) {
            for (Subject s : subjectCombo.getItems()) {
                if (s.getSubjectId() == task.getSubjectId()) {
                    subjectCombo.setValue(s);
                    break;
                }
            }
        }
        saveButton.setText("Actualizar");
    }

    public void handleSave(ActionEvent actionEvent) {
        // 1. Validaciones
        if (titleField.getText().isEmpty() || subjectCombo.getValue() == null) {
            formStatusLabel.setText("Título y Asignatura son obligatorios.");
            formStatusLabel.setTextFill(Color.RED);
            return;
        }

        try {
            Task task = (taskToEdit != null) ? taskToEdit : new Task();
            task.setTitle(titleField.getText());
            task.setDescriptionTask(descArea.getText());

            // 2. ID de Asignatura
            task.setSubjectId(subjectCombo.getValue().getSubjectId());

            // 3. Estado de la tarea
            task.setIsCompleted(completedCheckBox.isSelected());

            // 4. Fechas
            if (dueDatePicker.getValue() != null) {
                task.setDueDate(dueDatePicker.getValue());
            }

            // 5. Prioridad
            if (priorityCombo.getValue() != null) {
                task.setPriority(priorityCombo.getValue().toString().toUpperCase());
            }

            // 6. Persistencia
            if (taskToEdit != null) {
                taskService.updateTask(task.getTaskId(), task);
            } else {
                taskService.saveTask(task);
            }

            // Cerramos tras éxito
            closeWindow();

        } catch (Exception e) {
            formStatusLabel.setText("Error: " + e.getMessage());
            formStatusLabel.setTextFill(Color.RED);
            e.printStackTrace();
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