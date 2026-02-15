package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.service.TaskService;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class TaskOverviewController {
    @FXML public TableView<Task> taskTable;
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
            showAlert("Error", "No se pudieron cargar las tareas.");
        }
    }

    public void openCreateTaskDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/task_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("Nueva Tarea");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL); // Bloquea la ventana de atrás
            stage.showAndWait(); // Espera a que se cierre

            // Al cerrar, refrescamos la tabla para ver la nueva tarea
            loadTasks();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void handleDeleteTask(ActionEvent actionEvent) {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aviso", "Selecciona una tarea para borrar.");
            return;
        }

        // Confirmación (Usabilidad)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "¿Borrar tarea " + selected.getTitle() + "?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                taskService.deleteTask(selected.getTaskId());
                loadTasks(); // Refrescar tabla
            } catch (Exception e) {
                showAlert("Error", "No se pudo borrar la tarea.");
            }
        }
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}