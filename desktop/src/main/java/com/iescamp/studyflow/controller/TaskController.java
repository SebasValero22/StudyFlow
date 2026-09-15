package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.service.TaskService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.List;

public class TaskController {

    @FXML private TableView<Task> taskTable;
    // @FXML private TableColumn<Task, String> colTitle; // Asegúrate de tener las columnas en el FXML
    @FXML private Label statusLabel;

    private final TaskService taskService = new TaskService();

    @FXML
    public void initialize() {
        loadTasks();
    }

    private void loadTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            taskTable.setItems(FXCollections.observableArrayList(tasks));
        } catch (Exception e) {
            statusLabel.setText("Error al cargar tareas.");
            e.printStackTrace();
        }
    }

    // ESTE ES EL MÉTODO CORRECTO: Abrir el formulario para crear
    @FXML
    public void openCreateTaskDialog(ActionEvent event) {
        try {
            // Cargar el FXML del formulario (Asegúrate que la ruta sea correcta)
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/task_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nueva Tarea");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana de atrás
            stage.showAndWait(); // Espera a que se cierre

            // Al volver, refrescamos la tabla
            loadTasks();

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Error al abrir el formulario.");
        }
    }
}