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

    private final TaskService taskService = new TaskService();
    private final SubjectService subjectService = new SubjectService();

    @FXML
    public void initialize() {
        // 1. Cargar Prioridades
        priorityCombo.setItems(FXCollections.observableArrayList(Priority.values()));

        // 2. Cargar Asignaturas
        loadSubjects();
    }

    private void loadSubjects() {
        try {
            List<Subject> subjects = subjectService.getAllSubjects();
            subjectCombo.setItems(FXCollections.observableArrayList(subjects));

            // Convertidor para mostrar solo el nombre
            subjectCombo.setConverter(new StringConverter<>() {
                @Override
                public String toString(Subject s) { return (s != null) ? s.getNameSubject() : ""; }
                @Override
                public Subject fromString(String string) { return null; }
            });

        } catch (Exception e) {
            formStatusLabel.setText("Error cargando asignaturas: " + e.getMessage());
        }
    }

    public void handleSave(ActionEvent actionEvent) {
        // 1. Validaciones
        if (titleField.getText().isEmpty() || subjectCombo.getValue() == null) {
            formStatusLabel.setText("Título y Asignatura son obligatorios.");
            formStatusLabel.setTextFill(Color.RED);
            return;
        }

        try {
            Task newTask = new Task();
            newTask.setTitle(titleField.getText());
            newTask.setDescriptionTask(descArea.getText());

            // 2. ID de Asignatura
            // Usamos el objeto del ComboBox para obtener el ID real de la DB
            newTask.setSubjectId(subjectCombo.getValue().getSubjectId());

            // 3. Estado de la tarea
            newTask.setIsCompleted(completedCheckBox.isSelected());

            // 4. Fechas (LocalDate es lo que espera tu modelo y la API)
            if (dueDatePicker.getValue() != null) {
                // No necesitas conversión extra si usas LocalDate en el modelo
                newTask.setDueDate(dueDatePicker.getValue());
            }

            // 5. Prioridad
            if (priorityCombo.getValue() != null) {
                newTask.setPriority(priorityCombo.getValue().toString().toUpperCase());
            }

            // 6. Persistencia
            taskService.saveTask(newTask);

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