package com.iescamp.studyflow.controller;

import javafx.event.ActionEvent;

import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;

import java.io.IOException;

public class MainDashboardController {
    @FXML public StackPane contentArea;
    @FXML public Label statusLabel;
    
    @FXML private Button btnOverview;
    @FXML private Button btnSubjects;
    @FXML private Button btnTasks;
    @FXML private Button btnExams;
    @FXML private Button btnGrades;

    @FXML
    public void initialize() {
        showOverview(null);
    }

    @FXML
    public void showOverview(Event event) {
        loadView("overview_view.fxml");
        setActiveButton(btnOverview);
    }

    @FXML
    public void showTasks(ActionEvent actionEvent) {
        loadView("tasks_overview.fxml");
        setActiveButton(btnTasks);
    }

    @FXML
    public void showSubjects(ActionEvent actionEvent) {
        loadView("subjects_view.fxml");
        setActiveButton(btnSubjects);
    }

    @FXML
    public void showExams(ActionEvent actionEvent) {
        loadView("exams_view.fxml");
        setActiveButton(btnExams);
    }

    @FXML
    public void showGrades(ActionEvent actionEvent) {
        loadView("grades_view.fxml");
        setActiveButton(btnGrades);
    }

    @FXML
    public void showProfile(Event event) {
        // We'll reuse user_config_view.fxml as the profile
        loadView("user_config_view.fxml");
        clearActiveButtons();
    }

    @FXML
    private void loadView(String fxmlFile) {
        try {
            String path = "/com/iescamp/studyflow/fxml/" + fxmlFile;
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent view = loader.load();
            contentArea.getChildren().setAll(view);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void setActiveButton(Button activeBtn) {
        clearActiveButtons();
        if (activeBtn != null) {
            activeBtn.getStyleClass().add("nav-button-active");
        }
    }

    private void clearActiveButtons() {
        btnOverview.getStyleClass().remove("nav-button-active");
        btnSubjects.getStyleClass().remove("nav-button-active");
        btnTasks.getStyleClass().remove("nav-button-active");
        btnExams.getStyleClass().remove("nav-button-active");
        btnGrades.getStyleClass().remove("nav-button-active");
    }

    @FXML
    public void showUserConfig(ActionEvent actionEvent) {
        loadView("user_config_view.fxml");
        clearActiveButtons();
    }
}