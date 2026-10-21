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
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
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
            showAlert("Notice", "Select a task to delete.");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Delete task " + selected.getTitle() + "?");
        Optional<ButtonType> result = confirm.showAndWait();

        if (result.isPresent() && result.get() == ButtonType.OK) {
            try {
                taskService.deleteTask(selected.getTaskId());
                loadTasks();
            } catch (Exception e) {
                showAlert("Error", "Could not delete task.");
            }
        }
    }

    @FXML
    public void exportTasksToICS(ActionEvent event) {
        try {
            List<Task> tasks = taskService.getAllTasks();
            if (tasks.isEmpty()) {
                showAlert("No Tasks", "No tasks to export.");
                return;
            }

            StringBuilder ics = new StringBuilder();
            ics.append("BEGIN:VCALENDAR\r\n");
            ics.append("VERSION:2.0\r\n");
            ics.append("PRODID:-//StudyFlow//Task Export//EN\r\n");

            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyyMMdd'T'HHmmss'Z'");

            for (Task task : tasks) {
                if (task.getDueDate() == null) continue;

                LocalDateTime start = task.getDueDate().atTime(
                        task.getStartDate() != null ? LocalTime.parse("09:00") : LocalTime.of(9, 0));
                LocalDateTime end = start.plusMinutes(task.getDuration() != null ? task.getDuration() : 60);

                String uid = "task-" + task.getTaskId() + "@studyflow.local";
                String dtStamp = LocalDateTime.now().format(dtf);
                String dtStart = start.format(dtf);
                String dtEnd = end.format(dtf);
                String summary = task.getTitle() != null ? escapeICS(task.getTitle()) : "Task";
                String description = task.getDescriptionTask() != null ? escapeICS(task.getDescriptionTask()) : "";
                if (task.getSubjectName() != null) {
                    description = "Subject: " + task.getSubjectName() + "\n" + description;
                }
                if (task.getMicroStep() != null) {
                    description = "Micro-step: " + task.getMicroStep() + "\n" + description;
                }

                ics.append("BEGIN:VEVENT\r\n");
                ics.append("UID:").append(uid).append("\r\n");
                ics.append("DTSTAMP:").append(dtStamp).append("\r\n");
                ics.append("DTSTART:").append(dtStart).append("\r\n");
                ics.append("DTEND:").append(dtEnd).append("\r\n");
                ics.append("SUMMARY:").append(summary).append("\r\n");
                ics.append("DESCRIPTION:").append(description).append("\r\n");
                ics.append("END:VEVENT\r\n");
            }

            ics.append("END:VCALENDAR\r\n");

            FileChooser fileChooser = new FileChooser();
            fileChooser.setTitle("Save Calendar Export");
            fileChooser.setInitialFileName("studyflow-tasks.ics");
            fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("iCalendar Files", "*.ics"));
            File file = fileChooser.showSaveDialog(taskTable.getScene().getWindow());
            if (file != null) {
                try (FileWriter writer = new FileWriter(file)) {
                    writer.write(ics.toString());
                }
                showAlert("Export Complete", "Tasks exported to " + file.getName());
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("Export Error", "Failed to export: " + e.getMessage());
        }
    }

    private String escapeICS(String input) {
        return input.replace("\\", "\\\\")
                    .replace(",", "\\,")
                    .replace(";", "\\;")
                    .replace("\n", "\\n")
                    .replace("\r", "");
    }

    private void showAlert(String title, String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).show();
    }
}