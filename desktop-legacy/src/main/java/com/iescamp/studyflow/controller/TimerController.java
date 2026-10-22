package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.model.Task;
import com.iescamp.studyflow.service.FocusService;
import com.iescamp.studyflow.service.TaskService;
import com.iescamp.studyflow.utils.AppSettings;
import com.iescamp.studyflow.utils.Notifier;
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
    @FXML private Label cycleLabel;
    @FXML private ComboBox<Task> taskComboBox;
    @FXML private Button startButton;
    @FXML private Button resetButton;
    @FXML private Button skipButton;

    private TaskService taskService;
    private Timeline timeline;

    private int minutes;
    private int seconds;
    private boolean isFocusMode = true;
    private boolean isRunning = false;
    /** Completed focus sessions in the current cycle (drives long breaks). */
    private int completedFocusSessions = 0;

    @FXML
    public void initialize() {
        taskService = new TaskService();
        loadTasks();
        resetToFocus(false);
        updateDisplay();

        taskComboBox.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> {
            startButton.setDisable(newV == null);
        });

        startButton.setDisable(true);
    }

    private AppSettings settings() {
        return AppSettings.get();
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
        AppSettings s = settings();
        if (isFocusMode) {
            modeLabel.setText("Focus · " + s.getFocusMinutes() + "m");
        } else if (isLongBreak()) {
            modeLabel.setText("Long break · " + s.getLongBreakMinutes() + "m");
        } else {
            modeLabel.setText("Break · " + s.getShortBreakMinutes() + "m");
        }
        cycleLabel.setText("Session " + Math.min(completedFocusSessions + 1, s.getCyclesBeforeLongBreak())
                + " / " + s.getCyclesBeforeLongBreak());
    }

    /** Long break follows every Nth completed focus session. */
    private boolean isLongBreak() {
        return !isFocusMode && completedFocusSessions > 0
                && completedFocusSessions % settings().getCyclesBeforeLongBreak() == 0;
    }

    private void resetToFocus(boolean keepCounter) {
        if (!keepCounter) {
            completedFocusSessions = 0;
        }
        isFocusMode = true;
        minutes = settings().getFocusMinutes();
        seconds = 0;
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

        resetToFocus(false);
        updateDisplay();
        applySoftLock(false);
    }

    /** Jump to the next phase. Records a partial session when meaningful (>60s). */
    @FXML
    private void skipPhase() {
        int elapsed = elapsedSeconds();
        if (elapsed > 60) {
            boolean wasFocus = isFocusMode;
            Task pinned = taskComboBox.getSelectionModel().getSelectedItem();
            persistSession(pinned != null ? pinned.getTaskId() : null, elapsed, wasFocus);
            if (wasFocus) {
                completedFocusSessions++;
            }
        }
        advancePhase();
        if (settings().isAutoStartBreaks() || (isFocusMode && settings().isAutoStartFocus())) {
            startTimer();
        }
    }

    private void handleTimerComplete() {
        isRunning = false;
        startButton.setText("Start");
        if (timeline != null) timeline.stop();
        applySoftLock(false);

        int durationSeconds = phaseLengthSeconds();
        Task pinned = taskComboBox.getSelectionModel().getSelectedItem();
        Integer pinnedTaskId = pinned != null ? pinned.getTaskId() : null;
        boolean wasFocus = isFocusMode;

        persistSession(pinnedTaskId, durationSeconds, wasFocus);
        if (wasFocus) {
            completedFocusSessions++;
            Notifier.notify("Focus complete", "Time for a break. Session saved to your timesheet.");
        } else {
            Notifier.notify("Break over", "Back to focus. You've got this.");
        }
        Notifier.chime();

        advancePhase();

        if (!isFocusMode && settings().isAutoStartBreaks()) {
            startTimer();
        } else if (isFocusMode && settings().isAutoStartFocus()) {
            startTimer();
        }
    }

    /** Full length of the current phase in seconds (settings-driven). */
    private int phaseLengthSeconds() {
        AppSettings s = settings();
        if (isFocusMode) return s.getFocusMinutes() * 60;
        return (isLongBreak() ? s.getLongBreakMinutes() : s.getShortBreakMinutes()) * 60;
    }

    private int elapsedSeconds() {
        return Math.max(0, phaseLengthSeconds() - (minutes * 60 + seconds));
    }

    private void advancePhase() {
        if (isFocusMode) {
            isFocusMode = false;
            if (isLongBreak()) {
                minutes = settings().getLongBreakMinutes();
            } else {
                minutes = settings().getShortBreakMinutes();
            }
            seconds = 0;
        } else {
            isFocusMode = true;
            minutes = settings().getFocusMinutes();
            seconds = 0;
        }
        updateDisplay();
    }

    private void persistSession(Integer taskId, int durationSeconds, boolean focus) {
        // Persist the session off the FX thread - timesheet data must never block UI
        Thread saver = new Thread(() -> {
            try {
                new FocusService().recordSession(taskId, durationSeconds, focus);
            } catch (Exception e) {
                System.err.println("Focus session save failed: " + e.getMessage());
            }
        }, "focus-session-saver");
        saver.setDaemon(true);
        saver.start();
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
