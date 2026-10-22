package com.iescamp.studyflow;

import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.service.TaskService;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class QuickCaptureView {

    private Stage captureStage;
    private TextField inputField;
    private TaskService taskService;

    public QuickCaptureView() {
        taskService = new TaskService();
        initStage();
    }

    private void initStage() {
        captureStage = new Stage();
        captureStage.initStyle(StageStyle.TRANSPARENT);
        captureStage.setAlwaysOnTop(true);

        inputField = new TextField();
        inputField.setPromptText("Capture a thought to Someday... (Press Enter to save)");
        inputField.setStyle("-fx-font-size: 18px; -fx-padding: 10px; -fx-background-radius: 10px; -fx-border-radius: 10px;");
        inputField.setPrefWidth(500);

        inputField.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                saveTask(inputField.getText());
            } else if (event.getCode() == KeyCode.ESCAPE) {
                hide();
            }
        });

        VBox layout = new VBox(inputField);
        layout.setPadding(new Insets(20));
        layout.setStyle("-fx-background-color: transparent;");

        Scene scene = new Scene(layout);
        scene.setFill(Color.TRANSPARENT);
        
        captureStage.setScene(scene);
        
        // Handle focus loss
        captureStage.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) {
                hide();
            }
        });
    }

    private void saveTask(String title) {
        if (title == null || title.trim().isEmpty()) {
            hide();
            return;
        }
        
        Task task = new Task();
        task.setTitle(title.trim());
        task.setIsCompleted(false);
        task.setPriority("MEDIUM");
        task.setOrderIndex(999);
        // Due date and lifeArea are left null to send to Inbox

        try {
            taskService.saveTask(task);
        } catch (Exception e) {
            e.printStackTrace();
        }

        hide();
    }

    public void show() {
        Platform.runLater(() -> {
            inputField.clear();
            captureStage.show();
            captureStage.toFront();
            inputField.requestFocus();
        });
    }

    public void hide() {
        Platform.runLater(() -> {
            captureStage.hide();
        });
    }
}
