package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.service.TaskService;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.collections.FXCollections;
import java.util.List;

public class TaskController {

    // Vinculación con FXML (RA1.1)
    @FXML private TableView<Task> taskTable;
    @FXML private TextField titleField;
    @FXML private TextArea descArea;
    @FXML private DatePicker dueDatePicker;
    @FXML private Label statusLabel; // Para feedback visual (RA2.3)

    private final TaskService taskService = new TaskService();

    @FXML
    public void initialize() {
        // Al iniciar, cargamos los datos (RA2.1)
        loadTasks();
    }

    @FXML
    public void onSaveTask() {
        // 1. Validación previa en el cliente (RA4.3)
        if (titleField.getText().trim().isEmpty()) {
            showNotification("El título es obligatorio", Alert.AlertType.WARNING);
            return;
        }

        // 2. Creación del objeto desde la interfaz
        Task newTask = new Task();
        newTask.setTitle(titleField.getText());
        newTask.setDescription(descArea.getText());
        newTask.setDue_date(dueDatePicker.getValue());
        newTask.setIsCompleted("NO");

        // 3. Gestión de la comunicación con la API (RA8.2)
        statusLabel.setText("Guardando en el servidor..."); // Feedback de estado [cite: 201, 238]

        try {
            taskService.saveTask(newTask);
            statusLabel.setText("Tarea guardada con éxito.");
            clearFields();
            loadTasks();
        } catch (Exception e) {
            // 4. Gestión de errores sin bloquear la app (RA2.3, RA5.3)
            statusLabel.setText("Error: No se pudo conectar con el servidor.");
            showNotification("Error técnico: " + e.getMessage(), Alert.AlertType.ERROR);
        }
    }

    private void loadTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            taskTable.setItems(FXCollections.observableArrayList(tasks));
        } catch (Exception e) {
            statusLabel.setText("Error al cargar la lista de tareas.");
        }
    }

    private void showNotification(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Notificación StudyFlow");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private void clearFields() {
        titleField.clear();
        descArea.clear();
        dueDatePicker.setValue(null);
    }
}
