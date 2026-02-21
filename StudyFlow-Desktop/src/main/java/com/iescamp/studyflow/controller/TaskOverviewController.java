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
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.util.List;
import java.util.Optional;

public class TaskOverviewController {
    @FXML public TableView<Task> taskTable;
    @FXML public TableColumn<Task, String> colRemaining;
    @FXML private TableColumn<Task, Void> colDone;
    
    private final TaskService taskService = new TaskService();
    private final com.iescamp.studyflow.service.SubjectService subjectService = new com.iescamp.studyflow.service.SubjectService();
    private java.util.Map<Integer, String> subjectColors = new java.util.HashMap<>();

    @FXML
    public void initialize() {
        loadSubjectColors();
        
        // 1. Botón TRUE/FALSE
        colDone.setCellFactory(param -> new javafx.scene.control.TableCell<>() {
            private final javafx.scene.control.Button btn = new javafx.scene.control.Button();
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    Task task = getTableView().getItems().get(getIndex());
                    boolean completed = task.getIsCompleted() != null && task.getIsCompleted();
                    btn.setText(completed ? "TRUE" : "FALSE");
                    btn.setStyle("-fx-background-color: " + (completed ? "#2ecc71" : "#bdc3c7") + "; -fx-text-fill: white; -fx-font-weight: bold; -fx-font-size: 10px;");
                    btn.setOnAction(event -> toggleTaskStatus(task));
                    setGraphic(btn);
                }
            }
        });

        // 2. Color de fondo según asignatura
        taskTable.setRowFactory(tv -> new javafx.scene.control.TableRow<>() {
            @Override
            protected void updateItem(Task item, boolean empty) {
                super.updateItem(item, empty);
                if (item == null || empty) {
                    setStyle("");
                } else {
                    String color = subjectColors.getOrDefault(item.getSubjectId(), "#ffffff");
                    setStyle("-fx-background-color: " + color + "33; -fx-border-color: " + color + "; -fx-border-width: 0 0 0 5;");
                }
            }
        });

        if (colRemaining != null) {
            colRemaining.setCellValueFactory(data -> 
                new javafx.beans.property.SimpleStringProperty(data.getValue().getDaysRemaining()));
        }
        loadTasks();
    }

    private void loadSubjectColors() {
        try {
            subjectService.getAllSubjects().forEach(s -> subjectColors.put(s.getSubjectId(), s.getColor()));
        } catch (Exception e) { e.printStackTrace(); }
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
        openForm(null);
    }

    @FXML
    public void handleEditTask(ActionEvent actionEvent) {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Aviso", "Select a task to modify.");
            return;
        }
        openForm(selected);
    }

    private void openForm(Task task) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/task_form.fxml"));
            Parent root = loader.load();

            if (task != null) {
                TaskFormController controller = loader.getController();
                controller.setTask(task); // We need to add this method to the controller
            }

            Stage stage = new Stage();
            stage.setTitle(task == null ? "New Task" : "Modify Task");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

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

    @FXML
    public void toggleTaskStatus(Task task) {
        try {
            task.setIsCompleted(!task.getIsCompleted());
            taskService.updateTask(task.getTaskId(), task);

            loadTasks();
        } catch (Exception e) {
            showAlert("Error", "Could not update task status.");
        }
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}