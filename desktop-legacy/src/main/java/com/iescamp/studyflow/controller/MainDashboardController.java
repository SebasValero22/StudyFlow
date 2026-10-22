package com.iescamp.studyflow.controller;

import com.iescamp.studyflow.service.SyncConflictService;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.util.Duration;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

public class MainDashboardController {
    @FXML public StackPane contentArea;
    @FXML public Label statusLabel;
    @FXML private VBox sidebarMenu;
    @FXML private Button btnOverview;
    @FXML private Button btnVault;
    @FXML private Button conflictsButton;

    private final Timeline conflictPoller = new Timeline(new KeyFrame(Duration.seconds(15),
            e -> refreshConflictBadge()));

    @FXML
    public void initialize() {
            showOverview(new ActionEvent(btnOverview, null));
            statusLabel.setText("Welcome to StudyFlow");
            conflictPoller.setCycleCount(Timeline.INDEFINITE);
            conflictPoller.play();
            refreshConflictBadge();
    }

    private void refreshConflictBadge() {
        int n = SyncConflictService.activeCount();
        conflictsButton.setVisible(n > 0);
        conflictsButton.setManaged(n > 0);
        conflictsButton.setText("\u26A0 " + n + " sync conflict" + (n == 1 ? "" : "s"));
    }

    @FXML
    public void showConflicts() {
        List<SyncConflictService.Conflict> conflicts = SyncConflictService.list();
        if (conflicts.isEmpty()) {
            statusLabel.setText("No sync conflicts");
            refreshConflictBadge();
            return;
        }
        StringBuilder sb = new StringBuilder();
        for (SyncConflictService.Conflict c : conflicts) {
            sb.append(c.entityType()).append(" #").append(c.entityId())
              .append(" \u2014 your change: \"").append(nullSafe(c.localSummary())).append('"')
              .append(" (").append(nullSafe(c.localUpdatedAt())).append(")\n")
              .append("   replaced by cloud: \"").append(nullSafe(c.remoteSummary())).append('"')
              .append(" (").append(nullSafe(c.remoteUpdatedAt())).append(")\n\n");
        }
        TextArea area = new TextArea(sb.toString());
        area.setEditable(false);
        area.setWrapText(true);
        area.setPrefSize(580, 320);

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Sync conflicts");
        alert.setHeaderText(conflicts.size() + " local change(s) were overwritten by newer cloud data.");
        alert.getDialogPane().setContent(area);
        ButtonType dismissAll = new ButtonType("Dismiss all");
        ButtonType close = new ButtonType("Close", ButtonBar.ButtonData.CANCEL_CLOSE);
        alert.getButtonTypes().setAll(dismissAll, close);
        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == dismissAll) {
            SyncConflictService.dismissAll();
            statusLabel.setText("Conflicts dismissed");
        }
        refreshConflictBadge();
    }

    private static String nullSafe(String s) {
        return s == null || s.isBlank() ? "(empty)" : s;
    }

    @FXML
    public void showOverview(Event event) {
        loadView("overview_view.fxml");
        updateUI(event, "Viewing: Dashboard");
    }

    @FXML
    public void showTasks(ActionEvent actionEvent) {
        loadView("tasks_overview.fxml");
        updateUI(actionEvent, "Viewing: My Tasks");
    }

    @FXML
    public void showTimer(ActionEvent actionEvent) {
        loadView("timer_view.fxml");
        updateUI(actionEvent, "Viewing: Animedoro Focus Engine");
    }

    @FXML
    public void showNotes(ActionEvent actionEvent) {
        loadView("notes_view.fxml");
        updateUI(actionEvent, "Viewing: Notes Vault");
    }

    @FXML
    public void showSubjects(ActionEvent actionEvent) {
        loadView("subjects_view.fxml");
        updateUI(actionEvent, "Viewing: Subjects");
    }

    @FXML
    public void showExams(ActionEvent actionEvent) {
        loadView("exams_view.fxml");
        updateUI(actionEvent, "Viewing: Upcoming Exams");
    }

    @FXML
    public void showGrades(ActionEvent actionEvent) {
        loadView("grades_view.fxml");
        updateUI(actionEvent, "Viewing: Academic Grades");
    }

    @FXML
    public void showVault(ActionEvent actionEvent) {
        loadView("vault_view.fxml");
        updateUI(actionEvent, "Viewing: Vault");
    }

    @FXML
    private void loadView(String fxmlFile) {
        try {
            // CORRECCIÓN: Usa la variable fxmlFile, no pongas "register_view.fxml" a mano
            String path = "/com/iescamp/studyflow/fxml/" + fxmlFile;

            if (getClass().getResource(path) == null) {
                // Este es el error que ves ahora, porque overview_view.fxml no existía
                System.err.println("ERROR! FXML file not found at: " + path);
                statusLabel.setText("Error: File " + fxmlFile + " not found.");
                return;
            }

            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);

        } catch (IOException e) {
            e.printStackTrace();
            statusLabel.setText("Critical error loading: " + fxmlFile);
        }
    }

    // Método auxiliar para limpiar el estilo de los botones y actualizar el label
    private void updateUI(Event event, String statusText) {
        statusLabel.setText(statusText);

        // Reset all buttons to default (theme-driven via style class)
        if (sidebarMenu != null) {
            for (javafx.scene.Node node : sidebarMenu.getChildren()) {
                if (node instanceof Button btn) {
                    btn.setStyle("");
                    btn.getStyleClass().remove("sf-nav-button-active");
                    if (!btn.getStyleClass().contains("sf-nav-button")) {
                        btn.getStyleClass().add("sf-nav-button");
                    }
                }
            }
        }

        // Highlight the selected button
        if (event != null && event.getSource() instanceof Button btn) {
            if (!btn.getStyleClass().contains("sf-nav-button-active")) {
                btn.getStyleClass().add("sf-nav-button-active");
            }
        }
    }

    @FXML
    public void showUserConfig(ActionEvent actionEvent) {
        loadView("user_config_view.fxml"); // Asegúrate de que el nombre coincida con tu archivo
        statusLabel.setText("Profile Settings");
    }
}