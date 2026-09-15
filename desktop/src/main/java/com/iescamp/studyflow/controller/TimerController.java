package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.service.FocusService;
import com.iescamp.studyflow.service.TaskService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.List;

public class TimerController {

    @FXML private Label timeLabel;
    @FXML private Label modeLabel;
    @FXML private ComboBox<Task> taskComboBox;
    @FXML private Button startButton;
    @FXML private Button resetButton;

    private TaskService taskService;
    private Timeline timeline;
    
    private int minutes = 40;
    private int seconds = 0;
    private boolean isFocusMode = true;
    private boolean isRunning = false;

    @FXML
    public void initialize() {
        taskService = new TaskService();
        loadTasks();
        updateDisplay();
        
        taskComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            startButton.setDisable(newV == null);
        });
        
        startButton.setDisable(true);
    }

    private void loadTasks() {
        try {
            List<Task> tasks = taskService.getAllTasks();
            tasks.removeIf(Task::getIsCompleted);
            taskComboBox.setItems(FXCollections.observableArrayList(tasks));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void updateDisplay() {
        timeLabel.setText(String.format("%02d:%02d", minutes, seconds));
        modeLabel.setText(isFocusMode ? "Focus Mode (40m)" : "Reward Mode (20m)");
    }

    @FXML
    private void toggleTimer() {
        if (!isRunning) {
            startTimer();
        } else {
            pauseTimer();
        }
    }

    private void startTimer() {
        isRunning = true;
        startButton.setText("Pause");
        applySoftLock(true);
        
        if (timeline != null) timeline.stop();
        
        timeline = new Timeline(new KeyFrame(Duration.seconds(1), e -> {
            if (seconds == 0) {
                if (minutes == 0) {
                    handleTimerComplete();
                } else {
                    minutes--;
                    seconds = 59;
                }
            } else {
                seconds--;
            }
            updateDisplay();
        }));
        timeline.setCycleCount(Timeline.INDEFINITE);
        timeline.play();
    }

    private void pauseTimer() {
        isRunning = false;
        startButton.setText("Resume");
        if (timeline != null) timeline.pause();
        applySoftLock(false);
    }

    @FXML
    private void resetTimer() {
        isRunning = false;
        startButton.setText("Start");
        if (timeline != null) timeline.stop();
        
        isFocusMode = true;
        minutes = 40;
        seconds = 0;
        updateDisplay();
        applySoftLock(false);
    }

    private void handleTimerComplete() {
        isRunning = false;
        startButton.setText("Start");
        timeline.stop();
        applySoftLock(false);

        int durationSeconds = isFocusMode ? 40 * 60 : 20 * 60;
        Task pinned = taskComboBox.getSelectionModel().getSelectedItem();
        Integer pinnedTaskId = pinned != null ? pinned.getTaskId() : null;
        boolean wasFocus = isFocusMode;

        // Persist the session off the FX thread - timesheet data must never block UI
        Thread saver = new Thread(() -> {
            try {
                new FocusService().recordSession(pinnedTaskId, durationSeconds, wasFocus);
            } catch (Exception e) {
                System.err.println("Focus session save failed: " + e.getMessage());
            }
        }, "focus-session-saver");
        saver.setDaemon(true);
        saver.start();

        if (isFocusMode) {
            isFocusMode = false;
            minutes = 20;
            seconds = 0;
            // Mark task as done? Or ask user.
            System.out.println("Focus complete!");
        } else {
            isFocusMode = true;
            minutes = 40;
            seconds = 0;
            System.out.println("Reward complete!");
        }
        updateDisplay();
    }

    private void applySoftLock(boolean lock) {
        Platform.runLater(() -> {
            Stage stage = (Stage) timeLabel.getScene().getWindow();
            if (stage != null) {
                if (lock && isFocusMode) {
                    stage.setMaximized(true);
                    stage.setAlwaysOnTop(true);
                } else {
                    stage.setMaximized(false);
                    stage.setAlwaysOnTop(false);
                }
            }
        });
    }
}
