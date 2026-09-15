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

import java.util.List;
import java.util.Optional;

public class TaskOverviewController {
    @FXML public TableView<Task> taskTable;
    @FXML private TableColumn<Task, String> titleColumn;
    @FXML private TableColumn<Task, Boolean> colTaskCompleted;
    private final TaskService taskService = new TaskService();

    @FXML
    public void initialize() {
        setupTable();
        loadTasks();
    }

    private void setupTable() {
        titleColumn.setCellFactory(column -> new TableCell<Task, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    Task task = getTableView().getItems().get(getIndex());
                    String color = task.getSubjectColor();
                    if (color != null && !color.isEmpty()) {
                        if (!color.startsWith("#")) color = "#" + color;
                        setStyle("-fx-text-fill: " + color + "; -fx-font-weight: bold;");
                    } else {
                        setStyle("");
                    }
                }
            }
        });

        colTaskCompleted.setCellValueFactory(data -> {
            Boolean completed = data.getValue().getIsCompleted();
            return new javafx.beans.property.SimpleBooleanProperty(completed != null && completed);
        });
        colTaskCompleted.setCellFactory(column -> new TableCell<Task, Boolean>() {
            private final CheckBox checkBox = new CheckBox();
            {
                checkBox.setOnAction(event -> {
                    Task task = getTableView().getItems().get(getIndex());
                    task.setIsCompleted(checkBox.isSelected());
                    try {
                        taskService.updateTask(task.getTaskId(), task);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setGraphic(null);
                } else {
                    checkBox.setSelected(item);
                    setGraphic(checkBox);
                }
            }
        });
    }

    private void loadTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            taskTable.setItems(FXCollections.observableArrayList(tasks));
        } catch (Exception e) {
            showAlert("Error", "Could not load tasks.");
        }
    }

    @FXML
    public void openEditTaskDialog(ActionEvent event) {
        Task selected = taskTable.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert("Warning", "Select a task to edit.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/task_form.fxml"));
            Parent root = loader.load();

            TaskFormController controller = loader.getController();
            controller.setTaskToEdit(selected);

            Stage stage = new Stage();
            stage.setTitle("Edit Task");
            stage.setScene(new Scene(root));
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();

            loadTasks();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void openCreateTaskDialog(ActionEvent actionEvent) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/com/iescamp/studyflow/fxml/task_form.fxml"));
            Parent root = loader.load();

            Stage stage = new Stage();
            stage.setTitle("New Task");
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
            showAlert("Notice", "Select a task to delete.");
            return;
        }

        // Confirmación (Usabilidad)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete task " + selected.getTitle() + "?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                taskService.deleteTask(selected.getTaskId());
                loadTasks(); // Refrescar tabla
            } catch (Exception e) {
                showAlert("Error", "Could not delete task.");
            }
        }
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}